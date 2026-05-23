package com.togezzer.restapi.server.controller;

import com.togezzer.restapi.exception.AlreadyInServerException;
import com.togezzer.restapi.exception.ServerNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.server.dto.JoinServerDTO;
import com.togezzer.restapi.server.ServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ServerControllerJoinTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServerService serverService;

    @Test
    void joinServer_returns200_andCallsServiceWithserverUuidFromPath() throws Exception {
        final UUID serverUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        final String body = this.objectMapper.writeValueAsString(new JoinServerDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", serverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());
    }

    @Test
    void joinServer_whenServerNotFound_returns404_andMessage() throws Exception {
        final UUID serverUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new ServerNotFoundException("serveur introuvable"))
                .when(this.serverService)
                .join(org.mockito.ArgumentMatchers.any(JoinServerDTO.class), org.mockito.ArgumentMatchers.eq(serverUuid));

        final String body = this.objectMapper.writeValueAsString(new JoinServerDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", serverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("serveur introuvable"));
    }

    @Test
    void joinServer_whenUserNotFound_returns404_andMessage() throws Exception {
        final UUID serverUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new UserNotFoundException("user introuvable"))
                .when(this.serverService)
                .join(org.mockito.ArgumentMatchers.any(JoinServerDTO.class), org.mockito.ArgumentMatchers.eq(serverUuid));

        final String body = this.objectMapper.writeValueAsString(new JoinServerDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", serverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("user introuvable"));
    }

    @Test
    void joinServer_whenAlreadyInserver_returns400_andMessage() throws Exception {
        final UUID serverUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new AlreadyInServerException("déjà dans le serveur"))
                .when(this.serverService)
                .join(org.mockito.ArgumentMatchers.any(JoinServerDTO.class), org.mockito.ArgumentMatchers.eq(serverUuid));

        final String body = this.objectMapper.writeValueAsString(new JoinServerDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", serverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("déjà dans le serveur"));
    }

    @Test
    void joinServer_whenBodyMissingUserUuid_returns400_andDoesNotCallService() throws Exception {
        final UUID serverUuid = UUID.randomUUID();

        final String body = "{}";

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", serverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.serverService);
    }

    @Test
    void joinServer_whenserverUuidPathInvalid_returns400_andDoesNotCallService() throws Exception {
        final UUID userUuid = UUID.randomUUID();
        final String body = this.objectMapper.writeValueAsString(new JoinServerDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/server/{serverUuid}/join", "not-a-uuid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.serverService);
    }
}
