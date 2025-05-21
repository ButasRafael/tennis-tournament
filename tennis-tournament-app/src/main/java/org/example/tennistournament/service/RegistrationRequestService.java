package org.example.tennistournament.service;

import org.example.tennistournament.builder.RegistrationRequestBuilder;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.repository.RegistrationRequestRepository;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegistrationRequestService {

    private final RegistrationRequestRepository requestRepo;

    public RegistrationRequestService(RegistrationRequestRepository requestRepo) {
        this.requestRepo = requestRepo;
    }

    public RegistrationRequest createRequest(Tournament t, User p) {
        if(requestRepo.existsByTournamentIdAndPlayerId(t.getId(), p.getId())) {
            throw new RuntimeException("Request already exists for this player in this tournament");
        }
        RegistrationRequest req = RegistrationRequestBuilder.builder()
                .tournament(t)
                .player(p)
                .status(RegistrationRequest.Status.PENDING)
                .build();
        return requestRepo.save(req);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationRequest> listByStatus(RegistrationRequest.Status status) {
        return requestRepo.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RegistrationRequest approve(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (req.getStatus() != RegistrationRequest.Status.PENDING) {
            throw new IllegalStateException(
                    "Cannot approve a request that is already " + req.getStatus());
        }
        req.setStatus(RegistrationRequest.Status.APPROVED);
        return requestRepo.save(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RegistrationRequest deny(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (req.getStatus() != RegistrationRequest.Status.PENDING) {
            throw new IllegalStateException(
                    "Cannot deny a request that is already " + req.getStatus());
        }
        req.setStatus(RegistrationRequest.Status.DENIED);
        return requestRepo.save(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationRequest> listAll() {
        return requestRepo.findAll();
    }
}
