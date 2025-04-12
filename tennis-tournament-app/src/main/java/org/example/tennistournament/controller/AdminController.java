package org.example.tennistournament.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tennistournament.export.CSVExportStrategy;
import org.example.tennistournament.export.ExportService;
import org.example.tennistournament.export.TXTExportStrategy;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.service.TennisMatchService;
import org.example.tennistournament.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations for managing users and exporting match data")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TennisMatchService tennisMatchService;

    @Autowired
    private ExportService exportService;

    @GetMapping("/users")
    @Operation(summary = "Get all users (admin only)", description = "Retrieves a list of all users, ensuring the caller is an ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "ID of the current ADMIN user", required = true) @RequestParam Long currentUserId
    ) {
        try {
            userService.ensureAdminAccess(currentUserId);
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user (admin only)", description = "Deletes a user by ID if the caller is ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Deletion failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true) @PathVariable Long id,
            @Parameter(description = "ID of the current ADMIN user", required = true) @RequestParam Long currentUserId
    ) {
        try {
            userService.ensureAdminAccess(currentUserId);
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/export")
    @Operation(summary = "Export match data", description = "Exports matches for a given tournament in CSV or TXT format (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matches exported successfully"),
            @ApiResponse(responseCode = "400", description = "Export failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> exportMatches(
            @Parameter(description = "Export format (csv or txt)", required = true) @RequestParam String format,
            @Parameter(description = "Tournament ID", required = true) @RequestParam Long tournamentId,
            @Parameter(description = "ID of the current ADMIN user", required = true) @RequestParam Long currentUserId
    ) {
        try {
            userService.ensureAdminAccess(currentUserId);
            List<TennisMatch> matches = tennisMatchService.getMatchesByTournament(tournamentId);
            if ("csv".equalsIgnoreCase(format)) {
                exportService.setStrategy(new CSVExportStrategy());
            } else if ("txt".equalsIgnoreCase(format)) {
                exportService.setStrategy(new TXTExportStrategy());
            } else {
                throw new RuntimeException("Unsupported format: " + format);
            }
            String fileContent = exportService.exportMatches(matches);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=matches." + format)
                    .body(fileContent);
        } catch (RuntimeException ex) {
            Sentry.captureException(ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
