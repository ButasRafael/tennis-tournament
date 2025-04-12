package org.example.tennistournament.repository;

import org.example.tennistournament.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT t FROM Tournament t JOIN t.players p WHERE p.id = :playerId")
    List<Tournament> findAllByPlayer(@Param("playerId") Long playerId);

}
