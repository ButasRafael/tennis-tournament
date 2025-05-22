package org.example.tennistournament.dto;

import java.time.LocalDateTime;
import org.example.tennistournament.model.TennisMatch;

public class MatchDto {
    public Long            id;
    public String          tournamentName;
    public Long            player1Id;
    public String          player1Username;
    public Long            player2Id;
    public String          player2Username;
    public String          score;
    public LocalDateTime   startTime;
    public LocalDateTime   endTime;

    public MatchDto(TennisMatch m) {
        this.id               = m.getId();
        this.tournamentName   = m.getTournament().getName();
        this.player1Id        = m.getPlayer1().getId();
        this.player1Username  = m.getPlayer1().getUsername();
        this.player2Id        = m.getPlayer2().getId();
        this.player2Username  = m.getPlayer2().getUsername();
        this.score            = m.getScore();
        this.startTime        = m.getStartTime();
        this.endTime          = m.getEndTime();
    }
}
