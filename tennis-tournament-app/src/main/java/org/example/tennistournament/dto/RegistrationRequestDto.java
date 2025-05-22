package org.example.tennistournament.dto;

import java.time.LocalDateTime;
import org.example.tennistournament.model.RegistrationRequest;

public class RegistrationRequestDto {
    public Long            id;
    public String          playerUsername;
    public String          tournamentName;
    public LocalDateTime   createdAt;
    public String status;

    public RegistrationRequestDto(RegistrationRequest r) {
        this.id             = r.getId();
        this.playerUsername = r.getPlayer().getUsername();
        this.tournamentName = r.getTournament().getName();
        this.createdAt      = r.getCreatedAt();
        this.status         = r.getStatus().name();
    }
}
