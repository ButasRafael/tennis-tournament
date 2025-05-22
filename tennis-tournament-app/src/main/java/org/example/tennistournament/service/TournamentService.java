package org.example.tennistournament.service;

import jakarta.transaction.Transactional;
import org.example.tennistournament.builder.TournamentBuilder;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRequestService requestService;

    @PreAuthorize("hasRole('ADMIN')")
    public Tournament createTournament(String name,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       LocalDate registrationDeadline,
                                       int maxPlayers,
                                       int minPlayers,
                                       Long currentUserId) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User (creator) not found!"));

        if (creator.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only ADMIN can create tournaments!");
        }

        if (!startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Tournament start date must be greater than the current date!");
        }
        if (!registrationDeadline.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "Registration deadline must be smaller than the tournament start date!");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before the end date!");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("Max players cannot be less than min players!");
        }

        Tournament t = TournamentBuilder.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .registrationDeadline(registrationDeadline)
                .maxPlayers(maxPlayers)
                .minPlayers(minPlayers)
                .build();

        return tournamentRepository.save(t);
    }

    @PreAuthorize("#playerId == principal.id and hasRole('PLAYER')")
    public RegistrationRequest registerPlayer(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tournament not found!"));

        if (tournament.isCancelled()) {
            throw new IllegalArgumentException(
                    "Tournament is cancelled, no new registrations allowed!");
        }
        if (tournament.getRegistrationDeadline() != null
                && LocalDate.now().isAfter(tournament.getRegistrationDeadline())) {
            throw new IllegalArgumentException("Registration deadline has passed!");
        }

        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!"));

        if (player.getRole() != Role.PLAYER) {
            throw new IllegalArgumentException("Only PLAYER role can register for tournaments!");
        }

        // note: approved players list, not pending requests
        if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
            throw new IllegalArgumentException("Tournament is at max capacity!");
        }

        ensureNoOverlap(tournament, player);

        // create a pending registration request
        return requestService.createRequest(tournament, player);
    }

    private void ensureNoOverlap(Tournament newT, User player) {
        List<Tournament> joined = tournamentRepository.findAllByPlayer(player.getId());
        for (Tournament existing : joined) {
            if (!existing.isCancelled()) {
                boolean endsBefore = existing.getEndDate().isBefore(newT.getStartDate());
                boolean startsAfter = existing.getStartDate().isAfter(newT.getEndDate());
                if (!(endsBefore || startsAfter)) {
                    throw new IllegalArgumentException("Cannot join overlapping tournaments!");
                }
            }
        }
    }

    @Transactional
    public void checkAndCancelIfNotEnoughPlayers(Long tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tournament not found!"));

        if (LocalDate.now().isAfter(t.getRegistrationDeadline())
                && t.getPlayers().size() < t.getMinPlayers()) {
            t.setCancelled(true);
            tournamentRepository.save(t);
        }
    }

    @PreAuthorize("isAuthenticated()")
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public boolean isRefereeOfTournament(Long tournamentId, Long userId) {
        return tournamentRepository.findById(tournamentId)
                .map(t -> t.getMatches().stream()
                        .anyMatch(m -> m.getReferee().getId().equals(userId)))
                .orElse(false);
    }

    @PreAuthorize("#playerId == principal.id and hasRole('PLAYER')")
    public List<Tournament> getApprovedTournamentsForPlayer(Long playerId) {
        return tournamentRepository.findAllApprovedByPlayer(playerId);
    }
}
