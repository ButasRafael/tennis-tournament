package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.dto.RegistrationRequestDto;
import org.example.tennistournament.dto.UserDto;
import org.example.tennistournament.export.CSVExportStrategy;
import org.example.tennistournament.export.ExportService;
import org.example.tennistournament.export.TXTExportStrategy;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.service.EmailService;
import org.example.tennistournament.service.RegistrationRequestService;
import org.example.tennistournament.service.TennisMatchService;
import org.example.tennistournament.service.TournamentService;
import org.example.tennistournament.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations for managing users, matches, exports, and registration requests")
public class AdminController {

    private final UserService userService;
    private final TennisMatchService matchService;
    private final ExportService exportService;
    private final RegistrationRequestService reqService;
    private final TournamentService tournamentService;
    private final EmailService emailService;

    public AdminController(UserService userService,
                           TennisMatchService matchService,
                           ExportService exportService,
                           RegistrationRequestService reqService,
                           TournamentService tournamentService,
                           EmailService emailService) {
        this.userService = userService;
        this.matchService = matchService;
        this.exportService = exportService;
        this.reqService = reqService;
        this.tournamentService = tournamentService;
        this.emailService = emailService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by ID")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/export")
    @Operation(summary = "Export match data", description = "Exports matches for a given tournament in CSV or TXT")
    public ResponseEntity<String> exportMatches(
            @RequestParam String format,
            @RequestParam Long tournamentId
    ) {
        List<TennisMatch> matches = matchService.getMatchesByTournament(tournamentId);

        // Select strategy
        switch (format.toLowerCase()) {
            case "csv":
                exportService.setStrategy(new CSVExportStrategy());
                break;
            case "txt":
                exportService.setStrategy(new TXTExportStrategy());
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }

        String content = exportService.exportMatches(matches);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=matches." + format)
                .body(content);
    }

    @GetMapping("/registration-requests")
    @Operation(summary = "List registration requests", description = "Returns all registration requests, optionally filtered by status and/or tournament")
    public List<RegistrationRequestDto> listRequests(
            @RequestParam(required = false) RegistrationRequest.Status status,
            @RequestParam(required = false) Long tournamentId
    ) {
        var requests = (status != null)
                ? reqService.listByStatus(status)
                : reqService.listAll();

        if (tournamentId != null) {
            requests = requests.stream()
                    .filter(r -> r.getTournament().getId().equals(tournamentId))
                    .collect(Collectors.toList());
        }

        return requests.stream()
                .map(RegistrationRequestDto::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/registration-requests/{id}/approve")
    @Operation(summary = "Approve registration request", description = "Approves a registration and notifies the player")
    public RegistrationRequestDto approveRequest(@PathVariable Long id) {
        var req = reqService.approve(id);
        var tour = req.getTournament();
        tour.getPlayers().add(req.getPlayer());
        tournamentService.save(tour);
        emailService.sendRegistrationOutcome(req.getPlayer(), tour, true);
        return new RegistrationRequestDto(req);
    }

    @PostMapping("/registration-requests/{id}/deny")
    @Operation(summary = "Deny registration request", description = "Denies a registration and notifies the player")
    public RegistrationRequestDto denyRequest(@PathVariable Long id) {
        var req = reqService.deny(id);
        emailService.sendRegistrationOutcome(req.getPlayer(), req.getTournament(), false);
        return new RegistrationRequestDto(req);
    }
}
