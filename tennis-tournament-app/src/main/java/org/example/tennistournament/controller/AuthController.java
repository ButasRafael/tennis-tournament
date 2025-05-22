package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.tennistournament.auth.AuthenticationService;
import org.example.tennistournament.dto.AuthResponseDto;
import org.example.tennistournament.dto.RefreshTokenDto;
import org.example.tennistournament.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Operations for user registration, login, and token refresh")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with username, email, password, and an optional role, then returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully and tokens returned"),
            @ApiResponse(responseCode = "400", description = "Registration failed")
    })
    public ResponseEntity<AuthResponseDto> register(
            @Parameter(description = "Username of the new user", required = true)
            @RequestParam String username,
            @Parameter(description = "Email address of the new user", required = true)
            @RequestParam String email,
            @Parameter(description = "Plaintext password for the new user", required = true)
            @RequestParam String password,
            @Parameter(description = "Role of the new user", example = "PLAYER")
            @RequestParam(defaultValue = "PLAYER") Role role
    ) {
        var tokens = authService.register(username, email, password, role);
        return ResponseEntity.ok(new AuthResponseDto(tokens));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user using username and password and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully and tokens returned"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<AuthResponseDto> login(
            @Parameter(description = "Username of the user", required = true)
            @RequestParam String username,
            @Parameter(description = "Password of the user", required = true)
            @RequestParam String password
    ) {
        var tokens = authService.authenticate(username, password);
        return ResponseEntity.ok(new AuthResponseDto(tokens));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh JWT token",
            description = "Refreshes the access token using the refresh token provided in the Authorization header"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token")
    })
    public ResponseEntity<RefreshTokenDto> refreshToken(
            @Parameter(description = "HTTP request containing the refresh token in the Authorization header", required = true)
            HttpServletRequest request
    ) {
        var tokens = authService.refreshToken(request);
        return ResponseEntity.ok(new RefreshTokenDto(tokens));
    }

}
