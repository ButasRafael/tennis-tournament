package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.dto.MatchDto;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.service.TennisMatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Operations for creating and managing tennis matches")
public class TennisMatchController {

    private final TennisMatchService tennisMatchService;

    public TennisMatchController(TennisMatchService tennisMatchService) {
        this.tennisMatchService = tennisMatchService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create match", description = "Creates a new tennis match for a given tournament and players")
    @ApiResponse(responseCode = "200", description = "Match created successfully")
    public MatchDto createMatch(
            @Parameter(description = "Tournament ID", required = true) @RequestParam Long tournamentId,
            @Parameter(description = "Player1 ID",     required = true) @RequestParam Long player1Id,
            @Parameter(description = "Player2 ID",     required = true) @RequestParam Long player2Id,
            @Parameter(description = "Referee ID",     required = true) @RequestParam Long refereeId,

            @Parameter(description = "Start time (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime startTime,

            @Parameter(description = "End time (ISO-8601)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime endTime,

            @Parameter(description = "ID of the current user (must be ADMIN)", required = true)
            @RequestParam Long currentUserId
    ) {
        TennisMatch match = tennisMatchService.createMatch(
                tournamentId, player1Id, player2Id, refereeId,
                startTime, endTime, currentUserId
        );
        return new MatchDto(match);
    }

    @GetMapping("/tournament/{tournamentId}")
    @PreAuthorize("@tennisMatchService.isParticipantOrAdmin(#tournamentId, principal.id)")
    @Operation(summary = "Get matches by tournament", description = "Retrieves all matches for a tournament")
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    public List<MatchDto> getMatchesByTournament(
            @Parameter(description = "Tournament ID", required = true) @PathVariable Long tournamentId
    ) {
        return tennisMatchService.getMatchesByTournament(tournamentId)
                .stream()
                .map(MatchDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/referee/{refereeId}")
    @PreAuthorize("#refereeId == principal.id or hasRole('ADMIN')")
    @Operation(summary = "Get matches by referee", description = "Retrieves matches assigned to a specific referee")
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    public List<MatchDto> getMatchesByReferee(
            @Parameter(description = "Referee ID", required = true) @PathVariable Long refereeId
    ) {
        return tennisMatchService.getMatchesByReferee(refereeId)
                .stream()
                .map(MatchDto::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{matchId}/score")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('REFEREE') and @tennisMatchService.isRefereeOfMatch(#matchId, principal.id))")
    @Operation(summary = "Update match score", description = "Only the assigned referee (during match) or ADMIN can update the score")
    @ApiResponse(responseCode = "200", description = "Score updated successfully")
    public MatchDto updateMatchScore(
            @Parameter(description = "Match ID", required = true)   @PathVariable Long matchId,
            @Parameter(description = "New score (e.g., 6-4,3-6,7-5)", required = true)
            @RequestParam String newScore,
            @Parameter(description = "ID of the current user", required = true)
            @RequestParam Long currentUserId
    ) {
        TennisMatch updated = tennisMatchService.updateMatchScore(matchId, newScore, currentUserId);
        return new MatchDto(updated);
    }
}
