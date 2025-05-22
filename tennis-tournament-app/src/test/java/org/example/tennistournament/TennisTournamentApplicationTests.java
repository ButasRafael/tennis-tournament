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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
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
        Number idNum = (Number) jsonMap.get("id");
        Long userId = idNum.longValue();
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

        // second from same player must be rejected
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request already exists for this player in this tournament"));
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
                .andExpect(status().isForbidden());
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
                .andExpect(status().isForbidden());
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

        // register and approve first player
        MvcResult req1 = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> m1 = objectMapper.readValue(req1.getResponse().getContentAsString(), new TypeReference<>() {});
        Integer req1Id = (Integer) m1.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req1Id + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // register and approve second player
        var secondPlayerReg = registerUser("p2", "p2@xyz.com", "passabc", Role.PLAYER);
        Long secondPlayerId = secondPlayerReg.userId();
        String secondPlayerToken = secondPlayerReg.token();

        MvcResult req2 = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", secondPlayerId.toString())
                        .header("Authorization", "Bearer " + secondPlayerToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> m2 = objectMapper.readValue(req2.getResponse().getContentAsString(), new TypeReference<>() {});
        Integer req2Id = (Integer) m2.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req2Id + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // now create the first match successfully
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

        // overlapping should now fail
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

    @Test
    void testListAllRegistrationRequests() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Req Cup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // First request by existing player
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        // Second request by a new player
        var reg2 = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", reg2.userId().toString())
                        .header("Authorization", "Bearer " + reg2.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/registration-requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testListPendingRegistrationRequests() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Req Cup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        var reg2 = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", reg2.userId().toString())
                        .header("Authorization", "Bearer " + reg2.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/registration-requests")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testApproveRegistrationRequest() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Req Cup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // Create a pending request
        MvcResult create = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();
        String body = create.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
        Integer reqId = (Integer) map.get("id");

        mockMvc.perform(post("/api/admin/registration-requests/" + reqId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testDenyRegistrationRequest() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Req Cup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        MvcResult create = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();
        String body = create.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
        Integer reqId = (Integer) map.get("id");

        mockMvc.perform(post("/api/admin/registration-requests/" + reqId + "/deny")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DENIED"));
    }

    // --- New tests for referee filtering ---

    @Test
    void testRefereeFilterPlayersByUsername() throws Exception {
        var alice = registerUser("alice", "alice@xyz.com", "pass123", Role.PLAYER);
        var bob   = registerUser("bob",   "bob@xyz.com",   "pass123", Role.PLAYER);

        mockMvc.perform(get("/api/referee/players")
                        .param("username", "ali")
                        .header("Authorization", "Bearer " + refToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void testRefereeFilterPlayersByTournament() throws Exception {
        // 1) create tournament
        Tournament t = TournamentBuilder.builder()
                .name("Filter Cup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // 2) register & approve first player
        MvcResult r1 = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> m1 = objectMapper.readValue(
                r1.getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {});
        Integer req1 = (Integer) m1.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req1 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 3) register & approve second player
        RegisteredUser p2 = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);
        MvcResult r2 = mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", p2.userId().toString())
                        .header("Authorization", "Bearer " + p2.token()))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> m2 = objectMapper.readValue(
                r2.getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {});
        Integer req2 = (Integer) m2.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req2 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 4) assign our ref to that tournament by creating a dummy match
        LocalDateTime start = t.getStartDate().atTime(9, 0);
        LocalDateTime end   = t.getStartDate().atTime(10, 0);
        mockMvc.perform(post("/api/matches/create")
                        .param("tournamentId", t.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", p2.userId().toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", start.toString())
                        .param("endTime",   end.toString())
                        .param("currentUserId", adminId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 5) now as the assigned referee, filtering players in that tournament must succeed
        mockMvc.perform(get("/api/referee/players")
                        .param("tournamentId", t.getId().toString())
                        .header("Authorization", "Bearer " + refToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // --- UserController: getUser and updateUser ownership checks ---

    @Test
    void testGetOwnUser_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + playerId)
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId));
    }

    @Test
    void testGetOtherUser_Forbidden() throws Exception {
        // Player tries to fetch Admin’s info
        mockMvc.perform(get("/api/users/" + adminId)
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserByAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + playerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId));
    }

    @Test
    void testUpdateOtherUser_Forbidden() throws Exception {
        mockMvc.perform(put("/api/users/" + adminId)
                        .param("newUsername", "foo")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    // --- AdminController: getAllUsers ownership checks ---

    @Test
    void testGetAllUsers_NonAdminForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("currentUserId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsers_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("currentUserId", adminId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // --- TournamentController.registerPlayer ownership checks ---

    @Test
    void testTournamentRegistration_OtherPlayer_Forbidden() throws Exception {
        var p2 = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);
        Tournament t = TournamentBuilder.builder()
                .name("X")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // player1 tries to register player2
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", p2.userId().toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTournamentRegistration_Admin_Forbidden() throws Exception {
        Tournament t = TournamentBuilder.builder()
                .name("Y")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(10)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // admin tries to register a player
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // --- TennisMatchController: getMatchesByTournament and getMatchesByReferee ---

    @Test
    void testGetMatchesByTournament_Participant_Success() throws Exception {
        // 1) create tournament
        Tournament tour = TournamentBuilder.builder()
                .name("Cup1")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(2)
                .minPlayers(1)
                .build();
        tour = tournamentRepository.save(tour);

        // 2) register & approve player1
        MvcResult r1 = mockMvc.perform(post("/api/tournaments/" + tour.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> m1 = objectMapper.readValue(
                r1.getResponse().getContentAsString(),
                new TypeReference<>() {});
        Integer req1 = (Integer) m1.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req1 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 3) register & approve player2
        RegisteredUser p2 = registerUser("p2", "p2@xyz.com", "pass123", Role.PLAYER);
        MvcResult r2 = mockMvc.perform(post("/api/tournaments/" + tour.getId() + "/register")
                        .param("playerId", p2.userId().toString())
                        .header("Authorization", "Bearer " + p2.token()))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> m2 = objectMapper.readValue(
                r2.getResponse().getContentAsString(),
                new TypeReference<>() {});
        Integer req2 = (Integer) m2.get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req2 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 4) create a valid match
        LocalDateTime start = tour.getStartDate().atTime(9, 0);
        LocalDateTime end   = tour.getStartDate().atTime(10, 0);
        MvcResult matchR = mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", tour.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", p2.userId().toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", start.toString())
                        .param("endTime", end.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> mm = objectMapper.readValue(
                matchR.getResponse().getContentAsString(),
                new TypeReference<>() {});
        Long matchId = Long.valueOf((Integer) mm.get("id"));

        // 5) now participant can list matches by tournament
        mockMvc.perform(get("/api/matches/tournament/" + tour.getId())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }


    @Test
    void testGetMatchesByTournament_NonParticipant_Forbidden() throws Exception {
        // setup same as above
        Tournament tour = TournamentBuilder.builder()
                .name("Cup2")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(1)
                .minPlayers(1)
                .build();
        tour = tournamentRepository.save(tour);
        // no registration for p2
        var p2 = registerUser("p2x","p2x@x.com","pass123",Role.PLAYER);

        mockMvc.perform(get("/api/matches/tournament/" + tour.getId())
                        .header("Authorization", "Bearer " + p2.token()))
                .andExpect(status().isForbidden());
    }


    @Test
    void testGetMatchesByTournament_Admin_Success() throws Exception {
        // admin can list even with no matches
        Tournament tour = TournamentBuilder.builder()
                .name("Cup3")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(1)
                .minPlayers(1)
                .build();
        tour = tournamentRepository.save(tour);

        mockMvc.perform(get("/api/matches/tournament/" + tour.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMatchesByReferee_Self_Success() throws Exception {
        mockMvc.perform(get("/api/matches/referee/" + refereeId)
                        .header("Authorization", "Bearer " + refToken))
                .andExpect(status().isOk());
    }


    @Test
    void testGetMatchesByReferee_OtherForbidden() throws Exception {
        // refereeId is from @BeforeEach
        mockMvc.perform(get("/api/matches/referee/" + refereeId)
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void testGetMatchesByReferee_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/matches/referee/" + refereeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // --- TennisMatchController.updateMatchScore security checks ---

    @Test
    void testUpdateScore_ByPlayer_Forbidden() throws Exception {
        // assume matchId was created as above; reuse creation logic or extract into @BeforeEach helper
        Long matchId = createOneMatchAndReturnId();
        mockMvc.perform(put("/api/matches/" + matchId + "/score")
                        .param("newScore", "6-3,6-4")
                        .param("currentUserId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateScore_ByOtherReferee_Forbidden() throws Exception {
        Long matchId = createOneMatchAndReturnId();
        RegisteredUser r2 = registerUser("r2", "r2@xyz.com", "pass123", Role.REFEREE);
        mockMvc.perform(put("/api/matches/" + matchId + "/score")
                        .param("newScore", "6-3,6-4")
                        .param("currentUserId", r2.userId().toString())
                        .header("Authorization", "Bearer " + r2.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateScore_ByAdmin_Success() throws Exception {
        Long matchId = createOneMatchAndReturnId();
        mockMvc.perform(put("/api/matches/" + matchId + "/score")
                        .param("newScore", "6-3,6-4")
                        .param("currentUserId", adminId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value("6-3,6-4"));
    }

    // --- RefereeController.filterPlayers security checks ---

    @Test
    void testRefereeRoute_PlayerForbidden() throws Exception {
        mockMvc.perform(get("/api/referee/players")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRefereeRoute_AdminForbidden() throws Exception {
        mockMvc.perform(get("/api/referee/players")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRefereeRoute_TournamentNotAssigned_Forbidden() throws Exception {
        // create a tournament but do _not_ assign any match/referee to it
        Tournament t2 = TournamentBuilder.builder()
                .name("NoRefCup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(2)
                .minPlayers(1)
                .build();
        t2 = tournamentRepository.save(t2);

        mockMvc.perform(get("/api/referee/players")
                        .param("tournamentId", t2.getId().toString())
                        .header("Authorization", "Bearer " + refToken))
                .andExpect(status().isForbidden());
    }

    private Long createOneMatchAndReturnId() throws Exception {
        Tournament tour = TournamentBuilder.builder()
                .name("MatchCup")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(2)
                .minPlayers(1)
                .build();
        tour = tournamentRepository.save(tour);

        // register & approve both players (playerId and p2)
        MvcResult r1 = mockMvc.perform(post("/api/tournaments/" + tour.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk()).andReturn();
        Integer req1 = (Integer) objectMapper.readValue(r1.getResponse().getContentAsString(),
                new TypeReference<Map<String,Object>>(){}).get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req1 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        RegisteredUser p2 = registerUser("p2m","p2m@x.com","pass123",Role.PLAYER);
        MvcResult r2 = mockMvc.perform(post("/api/tournaments/" + tour.getId() + "/register")
                        .param("playerId", p2.userId().toString())
                        .header("Authorization", "Bearer " + p2.token()))
                .andExpect(status().isOk()).andReturn();
        Integer req2 = (Integer) objectMapper.readValue(r2.getResponse().getContentAsString(),
                new TypeReference<Map<String,Object>>(){}).get("id");
        mockMvc.perform(post("/api/admin/registration-requests/" + req2 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // finally create the match
        LocalDateTime s = tour.getStartDate().atTime(9,0);
        LocalDateTime e = tour.getStartDate().atTime(10,0);
        MvcResult mres = mockMvc.perform(post("/api/matches/create")
                        .param("currentUserId", adminId.toString())
                        .param("tournamentId", tour.getId().toString())
                        .param("player1Id", playerId.toString())
                        .param("player2Id", p2.userId().toString())
                        .param("refereeId", refereeId.toString())
                        .param("startTime", s.toString())
                        .param("endTime", e.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        Map<String,Object> mm = objectMapper.readValue(
                mres.getResponse().getContentAsString(),
                new TypeReference<>() {});
        return Long.valueOf((Integer) mm.get("id"));
    }

    @Test
    void testTournamentRegistration_DuplicateRequest() throws Exception {
        // 1) create tournament
        Tournament t = TournamentBuilder.builder()
                .name("Duplicate Cup")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .registrationDeadline(LocalDate.now().plusDays(1))
                .maxPlayers(5)
                .minPlayers(1)
                .build();
        t = tournamentRepository.save(t);

        // 2) first registration attempt
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk());

        // 3) second (duplicate) registration attempt should be rejected
        mockMvc.perform(post("/api/tournaments/" + t.getId() + "/register")
                        .param("playerId", playerId.toString())
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request already exists for this player in this tournament"));
    }

}

