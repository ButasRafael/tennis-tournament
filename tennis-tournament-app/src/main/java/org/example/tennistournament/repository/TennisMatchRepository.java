package org.example.tennistournament.repository;

import org.example.tennistournament.model.TennisMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TennisMatchRepository extends JpaRepository<TennisMatch, Long> {

    List<TennisMatch> findByTournamentId(Long tournamentId);
    List<TennisMatch> findByPlayer1Id(Long player1Id);
    List<TennisMatch> findByPlayer2Id(Long player2Id);
    List<TennisMatch> findByRefereeId(Long refereeId);

    @Query("""
       SELECT m FROM TennisMatch m
       WHERE (
         m.player1.id IN :participantIds
         OR m.player2.id IN :participantIds
         OR m.referee.id IN :participantIds
       )
       AND (
         m.startTime < :proposedEnd
         AND m.endTime > :proposedStart
       )
    """)
    List<TennisMatch> findOverlappingMatches(
            List<Long> participantIds,
            LocalDateTime proposedStart,
            LocalDateTime proposedEnd
    );
}
