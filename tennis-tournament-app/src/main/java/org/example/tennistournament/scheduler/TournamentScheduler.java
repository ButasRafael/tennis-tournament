package org.example.tennistournament.scheduler;

import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentScheduler {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentService tournamentService;

    // Runs every minute
    @Scheduled(cron = "0 * * * * *")
    public void checkTournamentsForMinPlayers() {
        List<Tournament> allTournaments = tournamentRepository.findAll();
        for (Tournament t : allTournaments) {
            if (!t.isCancelled()) {
                tournamentService.checkAndCancelIfNotEnoughPlayers(t.getId());
            }
        }
    }
}

