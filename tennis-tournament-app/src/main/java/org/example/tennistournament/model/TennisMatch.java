package org.example.tennistournament.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(
        name = "tennis_match",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_match_constraint",
                        columnNames = {"player1_id", "player2_id", "start_time"}
                )
        }
)
@Schema(description = "TennisMatch entity representing a match within a tournament")
public class TennisMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the match", example = "100")
    private Long id;

    @Version
    @Schema(description = "Version field for optimistic locking")
    private Long version;

    @ManyToOne
    @Schema(description = "Tournament this match belongs to")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player1_id")
    @Schema(description = "First player participating in the match")
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    @Schema(description = "Second player participating in the match")
    private User player2;

    @ManyToOne
    @Schema(description = "Referee assigned to the match")
    private User referee;

    @Schema(description = "Score of the match", example = "6-4,3-6,7-5")
    private String score;

    @Column(name = "start_time")
    @Schema(description = "Start time of the match", example = "2025-05-01T10:00:00")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @Schema(description = "End time of the match", example = "2025-05-01T12:00:00")
    private LocalDateTime endTime;

    public TennisMatch() {}

    public Long getId() {
        return id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public User getPlayer1() {
        return player1;
    }

    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public User getReferee() {
        return referee;
    }

    public void setReferee(User referee) {
        this.referee = referee;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
