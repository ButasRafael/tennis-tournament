package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.dto.UserDto;
import org.example.tennistournament.service.TournamentService;
import org.example.tennistournament.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/referee")
@Tag(name = "Referee", description = "Endpoints for referees")
public class RefereeController {

    private final UserService userService;
    private final TournamentService tournamentService;

    public RefereeController(UserService userService,
                             TournamentService tournamentService) {
        this.userService = userService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/players")
    @PreAuthorize(
            "hasRole('REFEREE') and " +
                    "(#tournamentId == null or @tournamentService.isRefereeOfTournament(#tournamentId, principal.id))"
    )
    @Operation(
            summary = "Filter players",
            description = "List PLAYER users, optionally only those in a tournament you referee"
    )
    public List<UserDto> filterPlayers(
            @Parameter(description = "Partial username to search (optional)")
            @RequestParam(required = false) String username,

            @Parameter(description = "Tournament ID to restrict to (optional)")
            @RequestParam(required = false) Long tournamentId
    ) {
        return userService
                .filterPlayers(username, tournamentId)
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
}
