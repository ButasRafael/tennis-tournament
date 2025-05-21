package org.example.tennistournament.repository;

import org.example.tennistournament.model.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    List<RegistrationRequest> findByStatus(RegistrationRequest.Status status);
    List<RegistrationRequest> findByTournamentId(Long tournamentId);
    boolean existsByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
}