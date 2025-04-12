package org.example.tennistournament.builder;

import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;

import java.time.LocalDateTime;

public class TennisMatchBuilder {

    private Tournament tournament;
    private User player1;
    private User player2;
    private User referee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String score = "";

    private TennisMatchBuilder() {}

    public static TennisMatchBuilder builder() {
        return new TennisMatchBuilder();
    }

    public TennisMatchBuilder tournament(Tournament tournament) {
        this.tournament = tournament;
        return this;
    }

    public TennisMatchBuilder player1(User player1) {
        this.player1 = player1;
        return this;
    }

    public TennisMatchBuilder player2(User player2) {
        this.player2 = player2;
        return this;
    }

    public TennisMatchBuilder referee(User referee) {
        this.referee = referee;
        return this;
    }

    public TennisMatchBuilder startTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public TennisMatchBuilder endTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public TennisMatchBuilder score(String score) {
        this.score = score;
        return this;
    }

    public TennisMatch build() {
        TennisMatch match = new TennisMatch();
        match.setTournament(tournament);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setReferee(referee);
        match.setStartTime(startTime);
        match.setEndTime(endTime);
        match.setScore(score);
        return match;
    }
}
