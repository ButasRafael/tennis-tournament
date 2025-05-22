package org.example.tennistournament.dto;

import java.time.LocalDate;
import org.example.tennistournament.model.Tournament;

public class TournamentDto {
    public Long        id;
    public String      name;
    public LocalDate   startDate;
    public LocalDate   endDate;
    public int         minPlayers;
    public int         maxPlayers;
    public boolean     cancelled;

    public TournamentDto(Tournament t) {
        this.id          = t.getId();
        this.name        = t.getName();
        this.startDate   = t.getStartDate();
        this.endDate     = t.getEndDate();
        this.minPlayers  = t.getMinPlayers();
        this.maxPlayers  = t.getMaxPlayers();
        this.cancelled   = t.isCancelled();
    }
}
