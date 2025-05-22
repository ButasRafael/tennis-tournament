package org.example.tennistournament.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Schema(description = "Tournament entity representing a tennis tournament")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the tournament", example = "10")
    private Long id;

    @Schema(description = "Name of the tournament", example = "Spring Open")
    private String name;

    @Schema(description = "Tournament start date", example = "2025-05-01")
    private LocalDate startDate;

    @Schema(description = "Tournament end date", example = "2025-05-10")
    private LocalDate endDate;

    @Schema(description = "Registration deadline for the tournament", example = "2025-04-25")
    private LocalDate registrationDeadline;

    @Schema(description = "Maximum number of players allowed", example = "32")
    private Integer maxPlayers = 32;

    @Schema(description = "Minimum number of players required", example = "2")
    private Integer minPlayers = 2;

    @Schema(description = "Indicates whether the tournament is cancelled")
    private boolean cancelled = false;

    @ManyToMany
    @JoinTable(
            name = "tournament_players",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Schema(description = "List of players registered for the tournament")
    private List<User> players = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    @Schema(description = "List of tennis matches in the tournament")
    private List<TennisMatch> matches = new ArrayList<>();

    public Tournament() {}

    // Getters and setters...
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<User> getPlayers() {
        return players;
    }

    public List<TennisMatch> getMatches() {
        return matches;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isCancelled() {
        return cancelled;
    }
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }


}
