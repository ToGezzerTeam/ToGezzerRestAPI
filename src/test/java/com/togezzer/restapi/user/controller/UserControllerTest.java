package com.togezzer.restapi.user.controller;

import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.user.UserService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    private String authHeader(final UUID userUuid) {
        return TestAuthTokenFactory.createBearerToken(jwtService, userUuid);
    }

    @Test
    void shouldReturnAllServersForAUser() throws Exception {
        final var userUuid = UUID.randomUUID();
        final var firstServer = createServerDTO(1L, UUID.randomUUID(), "alpha", true, Instant.parse("2026-01-01T10:00:00Z"));
        final var secondServer = createServerDTO(2L, UUID.randomUUID(), "beta", false, Instant.parse("2026-01-01T11:00:00Z"));

        doReturn(List.of(firstServer, secondServer)).when(userService).getAllUserServers();

        mockMvc.perform(get("/api/users/servers")
                        .header("Authorization", authHeader(userUuid)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].uuid").value(firstServer.getUuid().toString()))
                .andExpect(jsonPath("$[0].createdAt").value(firstServer.getCreatedAt().toString()))
                .andExpect(jsonPath("$[0].createdBy").value("user"))
                .andExpect(jsonPath("$[0].name").value("alpha"))
                .andExpect(jsonPath("$[0].public").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].uuid").value(secondServer.getUuid().toString()))
                .andExpect(jsonPath("$[1].name").value("beta"))
                .andExpect(jsonPath("$[1].public").value(false));

        verify(userService).getAllUserServers();
    }

    @Test
    void shouldReturn401WhenMissingAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/servers"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    private ServerDTO createServerDTO(final Long id, final UUID uuid, final String name, final boolean isPublic, final Instant createdAt) {
        return ServerDTO.builder()
                .id(id)
                .uuid(uuid)
                .createdAt(createdAt)
                .createdBy("user")
                .name(name)
                .isPublic(isPublic)
                .logo(name + "-logo")
                .background(name + "-background")
                .build();
    }
}

