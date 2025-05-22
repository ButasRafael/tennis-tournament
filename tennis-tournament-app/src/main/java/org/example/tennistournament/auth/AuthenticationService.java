package org.example.tennistournament.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.example.tennistournament.model.Token;
import org.example.tennistournament.model.TokenType;
import org.example.tennistournament.model.User;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.repository.TokenRepository;
import org.example.tennistournament.repository.UserRepository;
import org.example.tennistournament.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 TokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Map<String, String> register(String username, String email, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists!");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty!");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user = userRepository.save(user);

        revokeAllUserTokens(user);
        return generateAndSaveTokens(user);
    }

    public Map<String, String> authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        revokeAllUserTokens(user);
        return generateAndSaveTokens(user);
    }

    public Map<String, String> refreshToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token missing or malformed");
        }
        String oldRefresh = header.substring(7);
        String username = jwtService.extractUsername(oldRefresh);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh token: missing subject");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!jwtService.isTokenValid(oldRefresh, user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired refresh token");
        }

        revokeAllUserTokens(user);
        return generateAndSaveTokens(user);
    }

    // --- helper to mint, persist, and return tokens as a map ---
    private Map<String, String> generateAndSaveTokens(User user) {
        String access  = jwtService.generateToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        saveToken(user, access,  TokenType.ACCESS);
        saveToken(user, refresh, TokenType.REFRESH);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken",  access);
        response.put("refreshToken", refresh);
        response.put("role",         user.getRole().name());
        response.put("username",     user.getUsername());
        response.put("email",        user.getEmail());
        response.put("id",           String.valueOf(user.getId()));
        response.put("version",      String.valueOf(user.getVersion()));
        return response;
    }

    private void saveToken(User user, String jwt, TokenType type) {
        Token t = new Token();
        t.setUser(user);
        t.setToken(jwt);
        t.setTokenType(type);
        t.setExpired(false);
        t.setRevoked(false);
        tokenRepository.save(t);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> tokens = tokenRepository.findAllValidTokenByUser(user.getId());
        tokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }
}
