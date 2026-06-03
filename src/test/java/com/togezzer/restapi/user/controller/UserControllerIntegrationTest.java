package com.togezzer.restapi.user.controller;

import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import com.togezzer.restapi.server.ServerEntity;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ServerUserRepository serverUserRepository;

    private String authHeader(final UUID userUuid) {
        return TestAuthTokenFactory.createBearerToken(jwtService, userUuid);
    }

    @Test
    void shouldReturnAllServersThroughTheFullWebStack() throws Exception {
        final var userUuid = UUID.randomUUID();
        final var now = Instant.parse("2026-02-01T08:00:00Z");

        final var firstServer = createServerEntity(1L, UUID.randomUUID(), "alpha", now);
        final var secondServer = createServerEntity(2L, UUID.randomUUID(), "beta", now.plusSeconds(3600));

        doReturn(List.of(
                ServerUserEntity.builder().server(firstServer).build(),
                ServerUserEntity.builder().server(secondServer).build()
        )).when(serverUserRepository).findAllByUser_Uuid(userUuid);

        mockMvc.perform(get("/api/users/servers")
                        .header("Authorization", authHeader(userUuid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].uuid").value(firstServer.getUuid().toString()))
                .andExpect(jsonPath("$[0].createdAt").value(firstServer.getCreatedAt().toString()))
                .andExpect(jsonPath("$[0].name").value("alpha"))
                .andExpect(jsonPath("$[0].public").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].uuid").value(secondServer.getUuid().toString()))
                .andExpect(jsonPath("$[1].name").value("beta"))
                .andExpect(jsonPath("$[1].public").value(false));
    }

    @Test
    void shouldReturnEmptyArrayWhenTheUserHasNoServers() throws Exception {
        final var userUuid = UUID.randomUUID();

        doReturn(List.of()).when(serverUserRepository).findAllByUser_Uuid(userUuid);

        mockMvc.perform(get("/api/users/servers")
                        .header("Authorization", authHeader(userUuid)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private ServerEntity createServerEntity(final Long id, final UUID uuid, final String name, final Instant createdAt) {
        return ServerEntity.builder()
                .id(id)
                .uuid(uuid)
                .createdAt(createdAt)
                .createdBy("user")
                .name(name)
                .isPublic(id == 1L)
                .logo(name + "-logo")
                .background(name + "-background")
                .build();
    }
}

