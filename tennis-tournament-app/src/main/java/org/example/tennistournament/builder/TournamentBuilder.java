package org.example.tennistournament.builder;

import org.example.tennistournament.model.Tournament;

import java.time.LocalDate;

public class TournamentBuilder {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;

    // Optional fields
    private Integer maxPlayers = 32;
    private Integer minPlayers = 2;
    private boolean cancelled = false;

    private TournamentBuilder() {}

    public static TournamentBuilder builder() {
        return new TournamentBuilder();
    }

    public TournamentBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TournamentBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public TournamentBuilder endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public TournamentBuilder registrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
        return this;
    }

    public TournamentBuilder maxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    public TournamentBuilder minPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
        return this;
    }

    public TournamentBuilder cancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public Tournament build() {
        Tournament t = new Tournament();
        t.setName(name);
        t.setStartDate(startDate);
        t.setEndDate(endDate);
        t.setRegistrationDeadline(registrationDeadline);
        t.setMaxPlayers(maxPlayers);
        t.setMinPlayers(minPlayers);
        t.setCancelled(cancelled);
        return t;
    }
}
