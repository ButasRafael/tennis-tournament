package org.example.tennistournament.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.service.EmailService;
import org.example.tennistournament.service.RegistrationRequestService;
import org.example.tennistournament.service.TennisMatchService;
import org.example.tennistournament.service.TournamentService;
import org.example.tennistournament.service.UserService;
import org.example.tennistournament.export.CSVExportStrategy;
import org.example.tennistournament.export.ExportService;
import org.example.tennistournament.export.TXTExportStrategy;
import org.springframework.http.HttpStatus;
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
    private final TennisMatchService tennisMatchService;
    private final ExportService exportService;
    private final RegistrationRequestService reqService;
    private final TournamentService tournamentService;
    private final EmailService emailService;

    public AdminController(UserService userService,
                           TennisMatchService tennisMatchService,
                           ExportService exportService,
                           RegistrationRequestService reqService,
                           TournamentService tournamentService,
                           EmailService emailService) {
        this.userService = userService;
        this.tennisMatchService = tennisMatchService;
        this.exportService = exportService;
        this.reqService = reqService;
        this.tournamentService = tournamentService;
        this.emailService = emailService;
    }

    // Existing endpoints
    @GetMapping("/users")
    @Operation(summary = "Get all users (admin only)", description = "Retrieves a list of all users, ensuring the caller is an ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> getAllUsers(@RequestParam Long currentUserId) {
        try {
            userService.ensureAdminAccess(currentUserId);
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user (admin only)", description = "Deletes a user by ID if the caller is ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Deletion failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestParam Long currentUserId) {
        try {
            userService.ensureAdminAccess(currentUserId);
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/export")
    @Operation(summary = "Export match data", description = "Exports matches for a given tournament in CSV or TXT format (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matches exported successfully"),
            @ApiResponse(responseCode = "400", description = "Export failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> exportMatches(@RequestParam String format,
                                           @RequestParam Long tournamentId,
                                           @RequestParam Long currentUserId) {
        try {
            userService.ensureAdminAccess(currentUserId);
            List<TennisMatch> matches = tennisMatchService.getMatchesByTournament(tournamentId);
            if ("csv".equalsIgnoreCase(format)) {
                exportService.setStrategy(new CSVExportStrategy());
            } else if ("txt".equalsIgnoreCase(format)) {
                exportService.setStrategy(new TXTExportStrategy());
            } else {
                throw new RuntimeException("Unsupported format: " + format);
            }
            String fileContent = exportService.exportMatches(matches);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=matches." + format)
                    .body(fileContent);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // New registration-request endpoints

    @GetMapping("/registration-requests")
    @Operation(summary = "List registration requests", description = "Returns all registration requests, optionally filtered by status and/or tournament")
    public ResponseEntity<List<RegistrationRequest>> listRequests(
            @RequestParam(required = false) RegistrationRequest.Status status,
            @RequestParam(required = false) Long tournamentId) {
        List<RegistrationRequest> reqs = (status != null)
                ? reqService.listByStatus(status)
                : reqService.listAll();
        if (tournamentId != null) {
            reqs = reqs.stream()
                    .filter(r -> r.getTournament().getId().equals(tournamentId))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(reqs);
    }

    @PostMapping("/registration-requests/{id}/approve")
    @Operation(summary = "Approve registration request", description = "Marks a pending registration as approved, enrolls the player, and notifies by email")
    public ResponseEntity<RegistrationRequest> approveRequest(@PathVariable Long id) {
        RegistrationRequest req = reqService.approve(id);
        Tournament t = req.getTournament();
        t.getPlayers().add(req.getPlayer());
        tournamentService.save(t);
        emailService.sendRegistrationOutcome(req.getPlayer(), t, true);
        return ResponseEntity.ok(req);
    }

    @PostMapping("/registration-requests/{id}/deny")
    @Operation(summary = "Deny registration request", description = "Marks a pending registration as denied and notifies by email")
    public ResponseEntity<RegistrationRequest> denyRequest(@PathVariable Long id) {
        RegistrationRequest req = reqService.deny(id);
        emailService.sendRegistrationOutcome(req.getPlayer(), req.getTournament(), false);
        return ResponseEntity.ok(req);
    }
}