package org.example.tennistournament;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tennistournament.builder.TournamentBuilder;
import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class TennisTournamentApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TournamentRepository tournamentRepository;

    private Long adminId;
    private String adminToken;
    private Long playerId;
    private String playerToken;
    private Long refereeId;
    private String refToken;

    record RegisteredUser(Long userId, String token) {}

    @BeforeEach
    void setup() throws Exception {
        var adminReg = registerUser("admin", "admin@xyz.com", "pass123", Role.ADMIN);
        adminId = adminReg.userId();
        adminToken = adminReg.token();

        var playerReg = registerUser("player", "player@xyz.com", "pass123", Role.PLAYER);
        playerId = playerReg.userId();
        playerToken = playerReg.token();

        var refReg = registerUser("ref", "ref@xyz.com", "pass123", Role.REFEREE);
        refereeId = refReg.userId();
        refToken = refReg.token();
    }

    private RegisteredUser registerUser(String username, String email, String password, Role role) throws Exception {
        var result = mockMvc.perform(post("/api/users/register")
                        .param("username", username)
                        .param("email", email)
                        .param("password", password)
                        .param("role", role.toString()))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> jsonMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
        Long userId = Long.valueOf((String) jsonMap.get("id"));
        String token = (String) jsonMap.get("accessToken");
        return new RegisteredUser(userId, token);
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "player")
                        .param("password", "wrongpass"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "player")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testDeleteUserWithoutAdminAccess() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + refereeId)
                        .param("currentUserId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUserWithAdminAccess() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + refereeId)
                        .param("currentUserId", adminId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void testTournamentRegistration_Success() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Spring Open")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(2)
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());
    }

    @Test
    void testTournamentRegistration_FullCapacity() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Summer Open")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tournament is at max capacity!"));
    }

    @Test
    void testTournamentRegistration_DeadlinePassed() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Fall Open")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .registrationDeadline(LocalDate.now().minusDays(1))
                .maxPlayers(2)
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration deadline has passed!"));
    }

    @Test
    void testCreateTournament_StartDateNotInFuture() throws Exception {
        mockMvc.perform(post("/api/tournaments/create")
                        .param("currentUserId", adminId.toString())
                        .param("name", "Invalid Tournament")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .param("registrationDeadline", LocalDate.now().minusDays(1).toString())
                        .param("maxPlayers", "32")
                        .param("minPlayers", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tournament start date must be greater than the current date!"));
    }

    @Test
    void testCreateTournament_RegistrationDeadlineAfterStartDate() throws Exception {
        mockMvc.perform(post("/api/tournaments/create")
                        .param("currentUserId", adminId.toString())
                        .param("name", "Invalid Tournament")
                        .param("startDate", LocalDate.now().plusDays(3).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .param("registrationDeadline", LocalDate.now().plusDays(3).toString())
                        .param("maxPlayers", "32")
                        .param("minPlayers", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration deadline must be smaller than the tournament start date!"));
    }

    @Test
    void testCreateTournament_StartAfterEnd() throws Exception {
        mockMvc.perform(post("/api/tournaments/create")
                        .param("currentUserId", adminId.toString())
                        .param("name", "Invalid Tournament")
                        .param("startDate", LocalDate.now().plusDays(6).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .param("registrationDeadline", LocalDate.now().plusDays(1).toString())
                        .param("maxPlayers", "32")
                        .param("minPlayers", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must be before the end date!"));
    }

    @Test
    void testCreateTournament_MaxPlayersLessThanMinPlayers() throws Exception {
        mockMvc.perform(post("/api/tournaments/create")
                        .param("currentUserId", adminId.toString())
                        .param("name", "Invalid Tournament")
                        .param("startDate", LocalDate.now().plusDays(2).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .param("registrationDeadline", LocalDate.now().plusDays(1).toString())
                        .param("maxPlayers", "1")
                        .param("minPlayers", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Max players cannot be less than min players!"));
    }

    @Test
    void testCreateTournament_NonAdminAccess() throws Exception {
        mockMvc.perform(post("/api/tournaments/create")
                        .param("currentUserId", playerId.toString())
                        .param("name", "Unauthorized Tournament")
                        .param("startDate", LocalDate.now().plusDays(2).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .param("registrationDeadline", LocalDate.now().plusDays(1).toString())
                        .param("maxPlayers", "32")
                        .param("minPlayers", "2")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only ADMIN can create tournaments!"));
    }

    @Test
    void testCreateMatch_NoAdminAccess() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Match Cup")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(3))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", playerId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", playerId.toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", LocalDateTime.now().plusDays(2).toString())
                        .param("endTime", LocalDateTime.now().plusDays(2).plusHours(2).toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only ADMIN can create matches!"));
    }

    @Test
    void testCreateMatch_Success_And_RefereeError() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Another Cup")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(3))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", playerId.toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", LocalDateTime.now().plusDays(2).toString())
                        .param("endTime", LocalDateTime.now().plusDays(2).plusHours(2).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Player1 and Player2 cannot be the same user!"));
    }

    @Test
    void testOverlappingMatchesConflict() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Overlap Cup")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        var secondPlayerReg = registerUser("p2", "p2@xyz.com", "passabc", Role.PLAYER);
        Long secondPlayerId = secondPlayerReg.userId();
        String secondPlayerToken = secondPlayerReg.token();

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", secondPlayerId.toString())
                        .header("Authorization", "Bearer " + secondPlayerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", secondPlayerId.toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", LocalDateTime.now().minusMinutes(5).toString())
                        .param("endTime", LocalDateTime.now().plusHours(2).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", secondPlayerId.toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", LocalDateTime.now().plusMinutes(90).toString())
                        .param("endTime", LocalDateTime.now().plusHours(3).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Scheduling conflict: participant(s) already have a match overlapping this time!"));
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        String newUsername = "playerUpdated";
        String newEmail = "playerUpdated@xyz.com";
        String newPassword = "newpass123";

        mockMvc.perform(put("/api/users/" + playerId)
                        .param("newUsername", newUsername)
                        .param("newEmail", newEmail)
                        .param("newPassword", newPassword)
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(newUsername))
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void testUpdateAccount_DuplicateUsername() throws Exception {
        var player2Reg = registerUser("player2", "player2@xyz.com", "pass123", Role.PLAYER);

        mockMvc.perform(put("/api/users/" + player2Reg.userId())
                        .param("newUsername", "player")
                        .param("newEmail", "player2@xyz.com")
                        .param("newPassword", "")
                        .header("Authorization", "Bearer " + player2Reg.token()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New username is already taken!"));
    }

    @Test
    void testUpdateAccount_DuplicateEmail() throws Exception {
        var player2Reg = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);

        mockMvc.perform(put("/api/users/" + player2Reg.userId())
                        .param("newUsername", "p2")
                        .param("newEmail", "player@xyz.com")
                        .param("newPassword", "")
                        .header("Authorization", "Bearer " + player2Reg.token()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New email is already taken!"));
    }

    @Test
    void testUpdateAccount_InvalidPassword() throws Exception {
        mockMvc.perform(put("/api/users/" + playerId)
                        .param("newUsername", "player")
                        .param("newEmail", "player@xyz.com")
                        .param("newPassword", "123")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password must be at least 6 characters!"));
    }

    @Test
    void testCreateMatch_OutsideTournamentDates() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("OutsideDateCup")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(3))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", playerId.toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", LocalDateTime.now().plusDays(5).toString())
                        .param("endTime", LocalDateTime.now().plusDays(5).plusHours(1).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Player1 and Player2 cannot be the same user!"));
    }

    @Test
    void testCreateMatch_RefereeCannotBeAPlayer() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("RefereeErrorCup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .registrationDeadline(LocalDate.now())
                .build();
        t = tournamentRepository.save(t);

        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", playerId.toString()) // same user
                        .param("refereeId", playerId.toString())  // also the same user
                        .param("startTime", LocalDateTime.now().plusDays(1).toString())
                        .param("endTime", LocalDateTime.now().plusDays(1).plusHours(2).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Player1 and Player2 cannot be the same user!"));
    }

    @Test
    void testAdminDeletesAnotherAdmin() throws Exception {
        var admin2Reg = registerUser("admin2", "admin2@xyz.com", "secret123", Role.ADMIN);
        Long admin2Id = admin2Reg.userId();

        mockMvc.perform(delete("/api/admin/users/" + admin2Id)
                        .param("currentUserId", adminId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        var res = mockMvc.perform(post("/api/users/login")
                        .param("username", "player")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
        String oldRefresh = (String) map.get("refreshToken");

        var refreshRes = mockMvc.perform(post("/api/users/refresh-token")
                        .header("Authorization", "Bearer " + oldRefresh))
                .andExpect(status().isOk())
                .andReturn();

        String refreshBody = refreshRes.getResponse().getContentAsString();
        Map<String, Object> refreshMap = objectMapper.readValue(refreshBody, new TypeReference<>() {});
        org.assertj.core.api.Assertions.assertThat(refreshMap.get("accessToken")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(refreshMap.get("refreshToken")).isNotNull();
    }

    @Test
    void testLogoutAndUseOldToken() throws Exception {
        var login = mockMvc.perform(post("/api/users/login")
                        .param("username", "player")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andReturn();
        String body = login.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
        String oldAccessToken = (String) map.get("accessToken");

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + playerId)
                        .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isForbidden());
    }
}
