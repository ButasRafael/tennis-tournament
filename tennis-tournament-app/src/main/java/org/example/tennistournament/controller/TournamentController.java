package org.example.tennistournament.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@Tag(name = "Tournaments", description = "Operations for managing tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tournament", description = "Creates a new tournament with the given details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tournament created successfully"),
            @ApiResponse(responseCode = "400", description = "Creation failed due to validation error")
    })
    public ResponseEntity<?> createTournament(
            @Parameter(description = "Tournament name", required = true) @RequestParam String name,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true) @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true) @RequestParam String endDate,
            @Parameter(description = "Registration deadline (YYYY-MM-DD)", required = true) @RequestParam String registrationDeadline,
            @Parameter(description = "Maximum players", required = true) @RequestParam int maxPlayers,
            @Parameter(description = "Minimum players", required = true) @RequestParam int minPlayers,
            @Parameter(description = "ID of the user creating the tournament", required = true) @RequestParam Long currentUserId
    ) {
        try {
            Tournament t = tournamentService.createTournament(
                    name,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    LocalDate.parse(registrationDeadline),
                    maxPlayers,
                    minPlayers,
                    currentUserId
            );
            return ResponseEntity.ok(t);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{tournamentId}/register")
    @PreAuthorize("#playerId == principal.id and hasRole('PLAYER')")   // ← tighten to self
    @Operation(summary = "Register player", description = "Registers a player to a tournament")
    public ResponseEntity<?> registerPlayer(
            @Parameter(description = "Tournament ID", required = true) @PathVariable Long tournamentId,
            @Parameter(description = "Player ID", required = true) @RequestParam Long playerId
    ) {
        try {
            RegistrationRequest req = tournamentService.registerPlayer(tournamentId, playerId);
            return ResponseEntity.ok(req);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all tournaments", description = "Retrieves a list of all tournaments")
    @ApiResponse(responseCode = "200", description = "Tournaments retrieved successfully")
    public ResponseEntity<?> getAllTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
