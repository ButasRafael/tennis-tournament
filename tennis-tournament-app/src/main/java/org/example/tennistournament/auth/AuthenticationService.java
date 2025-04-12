package org.example.tennistournament.auth;

import org.example.tennistournament.model.User;
import org.example.tennistournament.model.Token;
import org.example.tennistournament.model.TokenType;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.repository.UserRepository;
import org.example.tennistournament.repository.TokenRepository;
import org.example.tennistournament.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
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
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty!");
        }
        if (password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        saveAccessToken(savedUser, jwtToken);
        saveRefreshToken(savedUser, refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", jwtToken);
        response.put("refreshToken", refreshToken);
        response.put("role", savedUser.getRole().name());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("id", String.valueOf(savedUser.getId()));
        response.put("version", String.valueOf(savedUser.getVersion()));

        return response;
    }

    public Map<String, String> authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveAccessToken(user, jwtToken);
        saveRefreshToken(user, refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", jwtToken);
        response.put("refreshToken", refreshToken);
        response.put("role", user.getRole().name());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("id", String.valueOf(user.getId()));
        response.put("version", String.valueOf(user.getVersion()));

        return response;
    }

    public Map<String, String> refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Refresh token missing or malformed");
        }

        String oldRefreshToken = authHeader.substring(7);
        String username = jwtService.extractUsername(oldRefreshToken);

        if (username == null) {
            throw new RuntimeException("Invalid refresh token: missing subject");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(oldRefreshToken, user)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);

        saveAccessToken(user, newAccessToken);
        saveRefreshToken(user, newRefreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        return response;
    }


    private void saveAccessToken(User user, String jwtToken) {
        Token token = new Token();
        token.setUser(user);
        token.setToken(jwtToken);
        token.setTokenType(TokenType.ACCESS);
        token.setExpired(false);
        token.setRevoked(false);
        tokenRepository.save(token);
    }

    private void saveRefreshToken(User user, String jwtToken) {
        Token token = new Token();
        token.setUser(user);
        token.setToken(jwtToken);
        token.setTokenType(TokenType.REFRESH);
        token.setExpired(false);
        token.setRevoked(false);
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens == null || validTokens.isEmpty()) {
            return;
        }
        for (Token token : validTokens) {
            token.setExpired(true);
            token.setRevoked(true);
        }
        tokenRepository.saveAll(validTokens);
    }
}
