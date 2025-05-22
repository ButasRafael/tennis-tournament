package org.example.tennistournament.service;

import jakarta.persistence.OptimisticLockException;
import org.example.tennistournament.builder.TennisMatchBuilder;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.TennisMatchRepository;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class TennisMatchService {

    @Autowired
    private TennisMatchRepository tennisMatchRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public TennisMatch createMatch(Long tournamentId,
                                   Long player1Id,
                                   Long player2Id,
                                   Long refereeId,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime,
                                   Long currentUserId) {
        // --- fetch and perms ---
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Current user not found!"));
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only ADMIN can create matches!");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tournament not found!"));
        User p1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Player1 not found!"));
        User p2 = userRepository.findById(player2Id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Player2 not found!"));
        User ref = userRepository.findById(refereeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Referee not found!"));

        // --- validation (400) ---
        if (p1.getId().equals(p2.getId())) {
            throw new IllegalArgumentException("Player1 and Player2 cannot be the same user!");
        }
        if (!tournament.getPlayers().contains(p1) || !tournament.getPlayers().contains(p2)) {
            throw new IllegalArgumentException("Both players must be registered in the tournament!");
        }
        if (p1.getRole() != Role.PLAYER || p2.getRole() != Role.PLAYER) {
            throw new IllegalArgumentException("Both participants must have the PLAYER role!");
        }
        if (ref.getRole() != Role.REFEREE) {
            throw new IllegalArgumentException("Referee must have the REFEREE role!");
        }
        if (ref.getId().equals(p1.getId()) || ref.getId().equals(p2.getId())) {
            throw new IllegalArgumentException("Referee cannot also be one of the players!");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time!");
        }
        LocalDate matchDay = startTime.toLocalDate();
        if (matchDay.isBefore(tournament.getStartDate()) ||
                matchDay.isAfter(tournament.getEndDate())) {
            throw new IllegalArgumentException(
                    "Match must be scheduled within the tournament's start/end dates!");
        }

        checkOverlaps(Arrays.asList(p1.getId(), p2.getId(), ref.getId()), startTime, endTime);

        // --- build & save ---
        TennisMatch match = TennisMatchBuilder.builder()
                .tournament(tournament)
                .player1(p1)
                .player2(p2)
                .referee(ref)
                .startTime(startTime)
                .endTime(endTime)
                .score("")
                .build();
        try {
            return tennisMatchRepository.save(match);
        } catch (OptimisticLockException ex) {
            throw new IllegalStateException("Match was concurrently updated, please refresh!");
        }
    }

    private void checkOverlaps(List<Long> participantIds,
                               LocalDateTime proposedStart,
                               LocalDateTime proposedEnd) {
        List<TennisMatch> conflicts = tennisMatchRepository.findOverlappingMatches(
                participantIds, proposedStart, proposedEnd
        );
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Scheduling conflict: participant(s) already have a match overlapping this time!");
        }
    }

    @PreAuthorize("@tennisMatchService.isParticipantOrAdmin(#tournamentId, principal.id)")
    public List<TennisMatch> getMatchesByTournament(Long tournamentId) {
        // repository returns empty list if none; permission already checked
        return tennisMatchRepository.findByTournamentId(tournamentId);
    }

    @PreAuthorize("#refereeId == principal.id or hasRole('ADMIN')")
    public List<TennisMatch> getMatchesByReferee(Long refereeId) {
        return tennisMatchRepository.findByRefereeId(refereeId);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('REFEREE') and @tennisMatchService.isRefereeOfMatch(#matchId, principal.id))")
    public TennisMatch updateMatchScore(Long matchId, String newScore, Long currentUserId) {
        TennisMatch match = tennisMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Match not found!"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Current user not found!"));

        // only assigned referee (during match time) or admin
        if (currentUser.getRole() == Role.REFEREE) {
            if (!match.getReferee().getId().equals(currentUserId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You are not the assigned referee for this match!");
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(match.getStartTime()) || now.isAfter(match.getEndTime())) {
                throw new IllegalArgumentException("Cannot update score outside of match time!");
            }
        } else if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only ADMIN or the assigned referee can update match scores!");
        }

        // not after tournament end
        LocalDate today = LocalDate.now();
        if (match.getTournament() != null && today.isAfter(match.getTournament().getEndDate())) {
            throw new IllegalArgumentException(
                    "Cannot update score after the tournament's end date!");
        }

        // simple regex check
        if (!newScore.matches("^[0-9\\- ,]+$")) {
            throw new IllegalArgumentException("Score format invalid. Example: 6-4,3-6,7-5");
        }

        match.setScore(newScore);
        try {
            return tennisMatchRepository.save(match);
        } catch (OptimisticLockException ex) {
            throw new IllegalStateException("Match was concurrently updated, please refresh!");
        }
    }

    public boolean isParticipantOrAdmin(Long tournamentId, Long userId) {
        // admins always OK
        if (userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN).orElse(false)) {
            return true;
        }
        // otherwise must be one of the registered players
        return tournamentRepository.findById(tournamentId)
                .map(t -> t.getPlayers().stream()
                        .anyMatch(p -> p.getId().equals(userId)))
                .orElse(false);
    }

    public boolean isRefereeOfMatch(Long matchId, Long userId) {
        return tennisMatchRepository.findById(matchId)
                .map(m -> m.getReferee().getId().equals(userId))
                .orElse(false);
    }
}
