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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                                   Long currentUserId)
    {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found!"));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can create matches!");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found!"));
        User p1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new RuntimeException("Player1 not found!"));
        User p2 = userRepository.findById(player2Id)
                .orElseThrow(() -> new RuntimeException("Player2 not found!"));
        User ref = userRepository.findById(refereeId)
                .orElseThrow(() -> new RuntimeException("Referee not found!"));
        if (p1.getId().equals(p2.getId())) {
            throw new RuntimeException("Player1 and Player2 cannot be the same user!");
        }
        if (!tournament.getPlayers().contains(p1) || !tournament.getPlayers().contains(p2)) {
            throw new RuntimeException("Both players must be registered in the tournament!");
        }
        if (p1.getRole() != Role.PLAYER || p2.getRole() != Role.PLAYER) {
            throw new RuntimeException("Both participants must have the PLAYER role!");
        }
        if (ref.getRole() != Role.REFEREE) {
            throw new RuntimeException("Referee must have the REFEREE role!");
        }

        if (ref.getId().equals(p1.getId()) || ref.getId().equals(p2.getId())) {
            throw new RuntimeException("Referee cannot also be one of the players!");
        }
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("Start time cannot be after end time!");
        }
        LocalDate matchDay = startTime.toLocalDate();
        if (matchDay.isBefore(tournament.getStartDate()) ||
                matchDay.isAfter(tournament.getEndDate())) {
            throw new RuntimeException("Match must be scheduled within the tournament's start/end dates!");
        }

        checkOverlaps(Arrays.asList(p1.getId(), p2.getId(), ref.getId()), startTime, endTime);

        TennisMatch match = TennisMatchBuilder.builder()
                .tournament(tournament)
                .player1(p1)
                .player2(p2)
                .referee(ref)
                .startTime(startTime)
                .endTime(endTime)
                .score("")
                .build();

        return tennisMatchRepository.save(match);
    }

    private void checkOverlaps(List<Long> participantIds,
                               LocalDateTime proposedStart,
                               LocalDateTime proposedEnd)
    {
        List<TennisMatch> conflicts = tennisMatchRepository.findOverlappingMatches(
                participantIds, proposedStart, proposedEnd
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Scheduling conflict: participant(s) already have a match overlapping this time!");
        }
    }

    @PreAuthorize("@tennisMatchService.isParticipantOrAdmin(#tournamentId, principal.id)")
    public List<TennisMatch> getMatchesByTournament(Long tournamentId) {
        return tennisMatchRepository.findByTournamentId(tournamentId);
    }

    @PreAuthorize("#refereeId == principal.id or hasRole('ADMIN')")
    public List<TennisMatch> getMatchesByReferee(Long refereeId) {
        return tennisMatchRepository.findByRefereeId(refereeId);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('REFEREE') and @tennisMatchService.isRefereeOfMatch(#matchId, principal.id))")
    public TennisMatch updateMatchScore(Long matchId, String newScore, Long currentUserId) {
        try {
            TennisMatch match = tennisMatchRepository.findById(matchId)
                    .orElseThrow(() -> new RuntimeException("Match not found!"));

            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Current user not found!"));

            if (currentUser.getRole() == Role.REFEREE) {
                if (!match.getReferee().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("You are not the assigned referee for this match!");
                }
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(match.getStartTime()) || now.isAfter(match.getEndTime())) {
                    throw new RuntimeException("Cannot update score outside of match time!");
                }
            }
            else if (currentUser.getRole() != Role.ADMIN) {
                throw new RuntimeException("Only ADMIN or the assigned referee can update match scores!");
            }

            Tournament t = match.getTournament();
            if (t != null &&
                    java.time.LocalDate.now().isAfter(t.getEndDate())) {
                throw new RuntimeException("Cannot update score after the tournament's end date!");
            }

            if (!newScore.matches("^[0-9\\- ,]+$")) {
                throw new RuntimeException("Score format invalid. Example: 6-4,3-6,7-5");
            }

            match.setScore(newScore);
            return tennisMatchRepository.save(match);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Match was concurrently updated, please refresh!");
        }
    }

    public boolean isParticipantOrAdmin(Long tournamentId, Long userId) {
        if (userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN).orElse(false)) {
            return true;
        }
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
