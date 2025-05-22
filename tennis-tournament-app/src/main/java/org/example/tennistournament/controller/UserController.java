package org.example.tennistournament.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.dto.UserDto;
import org.example.tennistournament.model.User;
import org.example.tennistournament.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations about users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserDto getUser(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);
        return new UserDto(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates user information like username, email, and/or password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Update failed due to validation error")
    })
    public UserDto updateUser(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New username")     @RequestParam(required = false) String newUsername,
            @Parameter(description = "New email")        @RequestParam(required = false) String newEmail,
            @Parameter(description = "New password")     @RequestParam(required = false) String newPassword
    ) {
        User updated = userService.updateUser(id, newUsername, newEmail, newPassword);
        return new UserDto(updated);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users from the system")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
}
