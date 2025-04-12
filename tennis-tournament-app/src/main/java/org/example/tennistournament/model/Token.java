package org.example.tennistournament.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

/**
 * Token entity representing a JWT token issued to a user.
 */
@Entity
@Table(name = "token")
@Schema(description = "Token entity representing a JWT token issued to a user")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the token", example = "100")
    private Long id;

    @Schema(description = "JWT token string", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of token (e.g. BEARER)", example = "BEARER")
    private TokenType tokenType;

    @Schema(description = "Indicates whether the token is revoked")
    private boolean revoked;

    @Schema(description = "Indicates whether the token is expired")
    private boolean expired;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Schema(description = "User associated with this token")
    private User user;

    // No-argument constructor required by JPA
    public Token() {
    }

    // All-argument constructor
    public Token(Long id, String token, TokenType tokenType, boolean revoked, boolean expired, User user) {
        this.id = id;
        this.token = token;
        this.tokenType = tokenType;
        this.revoked = revoked;
        this.expired = expired;
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }
    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isRevoked() {
        return revoked;
    }
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expired;
    }
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
