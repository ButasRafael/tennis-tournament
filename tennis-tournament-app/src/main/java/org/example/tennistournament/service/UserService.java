package org.example.tennistournament.service;

import jakarta.persistence.OptimisticLockException;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.RegistrationRequestRepository;
import org.example.tennistournament.repository.TennisMatchRepository;
import org.example.tennistournament.repository.TournamentRepository;
import org.example.tennistournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TennisMatchRepository tennisMatchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationRequestRepository registrationRequestRepository;

    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public User updateUser(Long userId, String newUsername, String newEmail, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found!"));

            if (newUsername != null && !newUsername.isBlank()) {
                if (userRepository.existsByUsername(newUsername)
                        && !newUsername.equals(user.getUsername())) {
                    throw new IllegalArgumentException("New username is already taken!");
                }
                user.setUsername(newUsername);
            }

            if (newEmail != null && !newEmail.isBlank()) {
                if (userRepository.existsByEmail(newEmail)
                        && !newEmail.equals(user.getEmail())) {
                    throw new IllegalArgumentException("New email is already taken!");
                }
                user.setEmail(newEmail);
            }

            if (newPassword != null && !newPassword.isBlank()) {
                if (newPassword.length() < 6) {
                    throw new IllegalArgumentException("New password must be at least 6 characters!");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            return userRepository.save(user);
        } catch (OptimisticLockException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User was concurrently updated, please refresh!",
                    ex);
        }
    }

    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found!"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found!"));

            // delete all related matches
            List<TennisMatch> asP1 = tennisMatchRepository.findByPlayer1Id(id);
            List<TennisMatch> asP2 = tennisMatchRepository.findByPlayer2Id(id);
            List<TennisMatch> asRef = tennisMatchRepository.findByRefereeId(id);
            asP1.forEach(tennisMatchRepository::delete);
            asP2.forEach(tennisMatchRepository::delete);
            asRef.forEach(tennisMatchRepository::delete);

            registrationRequestRepository.deleteAllByPlayerId(id);
            // remove from any tournaments
            List<Tournament> joined = tournamentRepository.findAllByPlayer(id);
            for (Tournament t : joined) {
                t.getPlayers().remove(user);
                tournamentRepository.save(t);
            }

            userRepository.delete(user);
        } catch (OptimisticLockException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Concurrent update conflict while deleting user!",
                    ex);
        }
    }

    public void ensureAdminAccess(Long currentUserId) {
        User caller = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Caller not found!"));

        if (caller.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied: You are not an admin!");
        }
    }

    public List<User> filterPlayers(String usernamePart, Long tournamentId) {
        List<User> players = userRepository.findAllByRole(Role.PLAYER);

        if (usernamePart != null && !usernamePart.isBlank()) {
            String lc = usernamePart.toLowerCase();
            players = players.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(lc))
                    .collect(Collectors.toList());
        }

        if (tournamentId != null) {
            Tournament t = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Tournament not found: " + tournamentId));
            players = players.stream()
                    .filter(t.getPlayers()::contains)
                    .collect(Collectors.toList());
        }

        return players;
    }

}
