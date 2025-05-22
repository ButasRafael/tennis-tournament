package org.example.tennistournament.service;

import org.example.tennistournament.builder.RegistrationRequestBuilder;
import org.example.tennistournament.model.RegistrationRequest;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.RegistrationRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class RegistrationRequestService {

    private final RegistrationRequestRepository requestRepo;

    public RegistrationRequestService(RegistrationRequestRepository requestRepo) {
        this.requestRepo = requestRepo;
    }

    /**
     * Create a new pending request. 400 if already exists.
     */
    public RegistrationRequest createRequest(Tournament t, User p) {
        if (requestRepo.existsByTournamentIdAndPlayerId(t.getId(), p.getId())) {
            throw new IllegalArgumentException(
                    "Request already exists for this player in this tournament");
        }
        RegistrationRequest req = RegistrationRequestBuilder.builder()
                .tournament(t)
                .player(p)
                .status(RegistrationRequest.Status.PENDING)
                .build();
        return requestRepo.save(req);
    }

    /**
     * List all requests in a given status. Admin only.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationRequest> listByStatus(RegistrationRequest.Status status) {
        return requestRepo.findByStatus(status);
    }

    /**
     * Approve a pending request. 404 if not found, 400 if not pending.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public RegistrationRequest approve(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));
        if (req.getStatus() != RegistrationRequest.Status.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot approve a request that is already " + req.getStatus());
        }
        req.setStatus(RegistrationRequest.Status.APPROVED);
        return requestRepo.save(req);
    }

    /**
     * Deny a pending request. 404 if not found, 400 if not pending.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public RegistrationRequest deny(Long requestId) {
        RegistrationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));
        if (req.getStatus() != RegistrationRequest.Status.PENDING) {
            throw new IllegalArgumentException(
                    "Cannot deny a request that is already " + req.getStatus());
        }
        req.setStatus(RegistrationRequest.Status.DENIED);
        return requestRepo.save(req);
    }

    /**
     * List every request, regardless of status. Admin only.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationRequest> listAll() {
        return requestRepo.findAll();
    }
}
