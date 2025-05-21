package org.example.tennistournament.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.service.TennisMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Operations for creating and managing tennis matches")
public class TennisMatchController {

    @Autowired
    private TennisMatchService tennisMatchService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create match", description = "Creates a new tennis match for a given tournament and players")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match created successfully"),
            @ApiResponse(responseCode = "400", description = "Creation failed due to validation or scheduling conflicts")
    })
    public ResponseEntity<?> createMatch(
            @Parameter(description = "Tournament ID", required = true) @RequestParam Long tournamentId,
            @Parameter(description = "Player1 ID", required = true) @RequestParam Long player1Id,
            @Parameter(description = "Player2 ID", required = true) @RequestParam Long player2Id,
            @Parameter(description = "Referee ID", required = true) @RequestParam Long refereeId,
            @Parameter(description = "Start time (ISO-8601 format)", required = true) @RequestParam String startTime,
            @Parameter(description = "End time (ISO-8601 format)", required = true) @RequestParam String endTime,
            @Parameter(description = "ID of the current user (must be ADMIN)", required = true) @RequestParam Long currentUserId
    ) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            TennisMatch match = tennisMatchService.createMatch(
                    tournamentId, player1Id, player2Id, refereeId,
                    start, end, currentUserId
            );
            return ResponseEntity.ok(match);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/tournament/{tournamentId}")
    @PreAuthorize("@tennisMatchService.isParticipantOrAdmin(#tournamentId, principal.id)")
    @Operation(summary = "Get matches by tournament", description = "Retrieves all matches associated with a tournament")
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    public ResponseEntity<?> getMatchesByTournament(
            @Parameter(description = "Tournament ID", required = true) @PathVariable Long tournamentId
    ) {
        try {
            List<TennisMatch> matches = tennisMatchService.getMatchesByTournament(tournamentId);
            return ResponseEntity.ok(matches);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @GetMapping("/referee/{refereeId}")
    @PreAuthorize("#refereeId == principal.id or hasRole('ADMIN')")
    @Operation(summary = "Get matches by referee", description = "Retrieves matches assigned to a specific referee")
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    public ResponseEntity<?> getMatchesByReferee(
            @Parameter(description = "Referee ID", required = true) @PathVariable Long refereeId
    ) {
        try {
            List<TennisMatch> matches = tennisMatchService.getMatchesByReferee(refereeId);
            return ResponseEntity.ok(matches);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{matchId}/score")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('REFEREE') and @tennisMatchService.isRefereeOfMatch(#matchId, principal.id))")
    @Operation(summary = "Update match score", description = "Updates the score for a match; only the assigned referee or ADMIN can update within valid time limits")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Score updated successfully"),
            @ApiResponse(responseCode = "400", description = "Update failed due to invalid score format or unauthorized access")
    })
    public ResponseEntity<?> updateMatchScore(
            @Parameter(description = "Match ID", required = true) @PathVariable Long matchId,
            @Parameter(description = "New score (format: 6-4,3-6,7-5)", required = true) @RequestParam String newScore,
            @Parameter(description = "ID of the current user", required = true) @RequestParam Long currentUserId
    ) {
        try {
            TennisMatch updatedMatch = tennisMatchService.updateMatchScore(matchId, newScore, currentUserId);
            return ResponseEntity.ok(updatedMatch);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
