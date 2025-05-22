package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.dto.RegistrationRequestDto;
import org.example.tennistournament.dto.TournamentDto;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.service.TournamentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
@Tag(name = "Tournaments", description = "Operations for managing tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tournament", description = "Creates a new tournament with the given details")
    @ApiResponse(responseCode = "200", description = "Tournament created successfully")
    public TournamentDto createTournament(
            @Parameter(description = "Tournament name", required = true)
            @RequestParam String name,

            @Parameter(description = "Start date", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate startDate,

            @Parameter(description = "End date", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate endDate,

            @Parameter(description = "Registration deadline", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate registrationDeadline,

            @Parameter(description = "Maximum players", required = true)
            @RequestParam int maxPlayers,

            @Parameter(description = "Minimum players", required = true)
            @RequestParam int minPlayers,

            @Parameter(description = "ID of the user creating the tournament", required = true)
            @RequestParam Long currentUserId
    ) {
        Tournament t = tournamentService.createTournament(
                name, startDate, endDate, registrationDeadline,
                maxPlayers, minPlayers, currentUserId
        );
        return new TournamentDto(t);
    }

    @PostMapping("/{tournamentId}/register")
    @PreAuthorize("#playerId == principal.id and hasRole('PLAYER')")
    @Operation(summary = "Register player", description = "Registers a player to a tournament")
    public RegistrationRequestDto registerPlayer(
            @Parameter(description = "Tournament ID", required = true)
            @PathVariable Long tournamentId,

            @Parameter(description = "Player ID", required = true)
            @RequestParam Long playerId
    ) {
        RegistrationRequest req = tournamentService.registerPlayer(tournamentId, playerId);
        return new RegistrationRequestDto(req);
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all tournaments", description = "Retrieves a list of all tournaments")
    @ApiResponse(responseCode = "200", description = "Tournaments retrieved successfully")
    public List<TournamentDto> getAllTournaments() {
        return tournamentService.getAllTournaments()
                .stream()
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('PLAYER') and #playerId == principal.id")
    @Operation(
            summary = "Get approved tournaments for a player",
            description = "Returns only those tournaments for which the current player has an APPROVED registration"
    )
    public List<TournamentDto> getApprovedTournaments(
            @Parameter(description = "ID of the player", required = true)
            @RequestParam Long playerId
    ) {
        return tournamentService.getApprovedTournamentsForPlayer(playerId)
                .stream()
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }
}
