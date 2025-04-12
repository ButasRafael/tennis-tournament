package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.tennistournament.auth.AuthenticationService;
import org.example.tennistournament.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Operations for user registration, login, and token refresh")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with username, email, password, and an optional role, then returns JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully and tokens returned"),
            @ApiResponse(responseCode = "400", description = "Registration failed")
    })
    public ResponseEntity<Map<String, String>> register(
            @Parameter(description = "Username of the new user", required = true) @RequestParam String username,
            @Parameter(description = "Email address of the new user", required = true) @RequestParam String email,
            @Parameter(description = "Plaintext password for the new user", required = true) @RequestParam String password,
            @Parameter(description = "Role of the new user (optional, defaults to PLAYER)") @RequestParam(required = false) Role role) {

        if (role == null) {
            role = Role.PLAYER;
        }
        Map<String, String> response = authService.register(username, email, password, role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user using username and password and returns JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully and tokens returned"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<Map<String, String>> login(
            @Parameter(description = "Username of the user", required = true) @RequestParam String username,
            @Parameter(description = "Password of the user", required = true) @RequestParam String password) {

        Map<String, String> response = authService.authenticate(username, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Refreshes the JWT access token using a valid refresh token provided in the Authorization header")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token")
    })
    public ResponseEntity<Map<String, String>> refreshToken(
            @Parameter(description = "HTTP request containing the refresh token in the Authorization header", required = true) HttpServletRequest request)
            throws Exception {

        Map<String, String> response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
