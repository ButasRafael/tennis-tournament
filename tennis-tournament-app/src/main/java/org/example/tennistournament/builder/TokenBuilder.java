package org.example.tennistournament.builder;

import org.example.tennistournament.model.Token;
import org.example.tennistournament.model.TokenType;
import org.example.tennistournament.model.User;

public class TokenBuilder {

    private Long id;
    private String token;
    private TokenType tokenType;
    private boolean revoked;
    private boolean expired;
    private User user;

    private TokenBuilder() {
    }

    public static TokenBuilder builder() {
        return new TokenBuilder();
    }

    public TokenBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public TokenBuilder token(String token) {
        this.token = token;
        return this;
    }

    public TokenBuilder tokenType(TokenType tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public TokenBuilder revoked(boolean revoked) {
        this.revoked = revoked;
        return this;
    }

    public TokenBuilder expired(boolean expired) {
        this.expired = expired;
        return this;
    }

    public TokenBuilder user(User user) {
        this.user = user;
        return this;
    }

    public Token build() {
        Token tokenObj = new Token();
        tokenObj.setId(this.id);
        tokenObj.setToken(this.token);
        tokenObj.setTokenType(this.tokenType);
        tokenObj.setRevoked(this.revoked);
        tokenObj.setExpired(this.expired);
        tokenObj.setUser(this.user);
        return tokenObj;
    }
}
