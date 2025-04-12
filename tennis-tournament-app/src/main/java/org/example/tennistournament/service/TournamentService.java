package org.example.tennistournament.service;

import jakarta.transaction.Transactional;
import org.example.tennistournament.builder.TournamentBuilder;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    public Tournament createTournament(String name,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       LocalDate registrationDeadline,
                                       int maxPlayers,
                                       int minPlayers,
                                       Long currentUserId) {
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User (creator) not found!"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can create tournaments!");
        }

        if(!startDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("Tournament start date must be greater than the current date!");
        }

        if(!registrationDeadline.isBefore(startDate)) {
            throw new RuntimeException("Registration deadline must be smaller than the tournament start date!");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date must be before the end date!");
        }

        if (maxPlayers < minPlayers) {
            throw new RuntimeException("Max players cannot be less than min players!");
        }

        Tournament tournament = TournamentBuilder.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .registrationDeadline(registrationDeadline)
                .maxPlayers(maxPlayers)
                .minPlayers(minPlayers)
                .build();

        return tournamentRepository.save(tournament);
    }

    public Tournament registerPlayer(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found!"));

        if (tournament.isCancelled()) {
            throw new RuntimeException("Tournament is cancelled, no new registrations allowed!");
        }

        if (tournament.getRegistrationDeadline() != null
                && LocalDate.now().isAfter(tournament.getRegistrationDeadline())) {
            throw new RuntimeException("Registration deadline has passed!");
        }

        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (player.getRole() != Role.PLAYER) {
            throw new RuntimeException("Only PLAYER role can register for tournaments!");
        }

        if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
            throw new RuntimeException("Tournament is at max capacity!");
        }

        checkTournamentOverlap(tournament, player);

        if(tournament.getStartDate().isAfter(tournament.getEndDate())) {
            throw new RuntimeException("Tournament start date must be before end date!");
        }

        if (!tournament.getPlayers().contains(player)) {
            tournament.getPlayers().add(player);
            tournamentRepository.save(tournament);
        }

        return tournament;
    }

    private void checkTournamentOverlap(Tournament newTournament, User player) {
        List<Tournament> existing = tournamentRepository.findAllByPlayer(player.getId());
        for (Tournament t : existing) {
            if (!t.isCancelled()) {
                boolean endsBeforeNewStarts = t.getEndDate().isBefore(newTournament.getStartDate());
                boolean startsAfterNewEnds = t.getStartDate().isAfter(newTournament.getEndDate());
                if (!(endsBeforeNewStarts || startsAfterNewEnds)) {
                    throw new RuntimeException("Cannot join overlapping tournaments!");
                }
            }
        }
    }

    @Transactional
    public void checkAndCancelIfNotEnoughPlayers(Long tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found!"));

        if (LocalDate.now().isAfter(t.getRegistrationDeadline())) {
            if (t.getPlayers().size() < t.getMinPlayers()) {
                t.setCancelled(true);
                tournamentRepository.save(t);
            }
        }
    }
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

}
