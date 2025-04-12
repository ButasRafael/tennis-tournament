package org.example.tennistournament.service;

import jakarta.persistence.OptimisticLockException;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.TennisMatch;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.example.tennistournament.repository.TennisMatchRepository;
import org.example.tennistournament.repository.UserRepository;
import org.example.tennistournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

//    public User registerUser(String username, String email, String rawPassword, Role role) {
//        if (userRepository.existsByUsername(username)) {
//            throw new RuntimeException("Username already exists!");
//        }
//        if (userRepository.existsByEmail(email)) {
//            throw new RuntimeException("Email already exists!");
//        }
//
//        if (rawPassword == null || rawPassword.isBlank()) {
//            throw new RuntimeException("Password cannot be empty!");
//        }
//        if (rawPassword.length() < 6) {
//            throw new RuntimeException("Password must be at least 6 characters long!");
//        }
//
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        User user = UserBuilder.builder()
//                .username(username)
//                .email(email)
//                .password(encodedPassword)
//                .role(role)
//                .build();
//        return userRepository.save(user);
//    }
//
//    public User loginUser(String username, String rawPassword) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found!"));
//
//        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
//            throw new RuntimeException("Invalid password!");
//        }
//        return user;
//    }

    public User updateUser(Long userId, String newUsername, String newEmail, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            if (newUsername != null && !newUsername.isBlank()) {
                if (userRepository.existsByUsername(newUsername) && !newUsername.equals(user.getUsername())) {
                    throw new RuntimeException("New username is already taken!");
                }
                user.setUsername(newUsername);
            }

            if (newEmail != null && !newEmail.isBlank()) {
                if (userRepository.existsByEmail(newEmail) && !newEmail.equals(user.getEmail())) {
                    throw new RuntimeException("New email is already taken!");
                }
                user.setEmail(newEmail);
            }

            if (newPassword != null && !newPassword.isBlank()) {
                if (newPassword.length() < 6) {
                    throw new RuntimeException("New password must be at least 6 characters!");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            return userRepository.save(user);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("User was concurrently updated, please refresh!");
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            List<TennisMatch> matchesAsPlayer1 = tennisMatchRepository.findByPlayer1Id(user.getId());
            List<TennisMatch> matchesAsPlayer2 = tennisMatchRepository.findByPlayer2Id(user.getId());
            List<TennisMatch> matchesAsReferee = tennisMatchRepository.findByRefereeId(user.getId());

            for (TennisMatch match : matchesAsPlayer1) {
                tennisMatchRepository.delete(match);
            }
            for (TennisMatch match : matchesAsPlayer2) {
                tennisMatchRepository.delete(match);
            }
            for (TennisMatch match : matchesAsReferee) {
                tennisMatchRepository.delete(match);
            }

            List<Tournament> userTournaments = tournamentRepository.findAllByPlayer(user.getId());
            for (Tournament t : userTournaments) {
                t.getPlayers().remove(user);
                tournamentRepository.save(t);
            }
            userRepository.delete(user);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Concurrent update conflict while deleting user!");
        }
    }

    public void ensureAdminAccess(Long currentUserId) {
        User caller = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Caller not found!"));

        if (caller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied: You are not an admin!");
        }
    }

}
