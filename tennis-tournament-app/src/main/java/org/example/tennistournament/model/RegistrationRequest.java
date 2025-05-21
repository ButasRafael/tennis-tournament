package org.example.tennistournament.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "registration_request",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_request_per_player",
                columnNames = { "tournament_id", "player_id" }
        )
)
public class RegistrationRequest {

    public enum Status { PENDING, APPROVED, DENIED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private User player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
    public Long getId() { return id; }
    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    public User getPlayer() { return player; }
    public void setPlayer(User player) { this.player = player; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}