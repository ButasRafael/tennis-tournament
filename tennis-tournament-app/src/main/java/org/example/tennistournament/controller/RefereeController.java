package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.model.User;
import org.example.tennistournament.service.TournamentService;
import org.example.tennistournament.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referee")
@Tag(name = "Referee", description = "Endpoints for referees")
public class RefereeController {

    private final UserService userService;
    private final TournamentService tournamentService;   // ← inject

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
    @Operation(summary = "Filter players", description = "List PLAYER users, optionally only those in a tournament you referee")
    public List<User> filterPlayers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long tournamentId
    ) {
        return userService.filterPlayers(username, tournamentId);
    }
}
