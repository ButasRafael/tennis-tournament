package org.example.tennistournament.builder;

import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;

public class RegistrationRequestBuilder {
    private Tournament tournament;
    private User player;
    private RegistrationRequest.Status status = RegistrationRequest.Status.PENDING;

    public static RegistrationRequestBuilder builder() {
        return new RegistrationRequestBuilder();
    }

    public RegistrationRequestBuilder tournament(Tournament t) {
        this.tournament = t;
        return this;
    }
    public RegistrationRequestBuilder player(User u) {
        this.player = u;
        return this;
    }
    public RegistrationRequestBuilder status(RegistrationRequest.Status s) {
        this.status = s;
        return this;
    }

    public RegistrationRequest build() {
        RegistrationRequest req = new RegistrationRequest();
        req.setTournament(tournament);
        req.setPlayer(player);
        req.setStatus(status);
        return req;
    }
}
