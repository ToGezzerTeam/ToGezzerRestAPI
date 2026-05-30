package com.togezzer.restapi.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import com.togezzer.restapi.server.dto.RenameServerDTO;
import com.togezzer.restapi.server.dto.ServerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ServerRenameControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

    @Test
    void should_rename_server_successfully() throws Exception {
        // Create a server first so rename returns 200 (otherwise it returns 404)
        final var createRequest = createServerDTO();
        
        final var createResponse = mockMvc.perform(post("/api/servers")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var createdServer = objectMapper.readValue(createResponse, ServerDTO.class);
        assertNotNull(createdServer.getUuid());

        final var renameRequest = new RenameServerDTO("New name");

        mockMvc.perform(patch("/api/servers/{serverUuid}/rename", createdServer.getUuid())
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_400_when_newName_is_blank() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameServerDTO("   ");

        mockMvc.perform(patch("/api/servers/{serverUuid}/rename", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_body_is_missing() throws Exception {
        final var uuid = UUID.randomUUID();

        mockMvc.perform(patch("/api/servers/{serverUuid}/rename", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_404_when_server_does_not_exist() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameServerDTO("New name");

        mockMvc.perform(patch("/api/servers/{serverUuid}/rename", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("does not exist")));
    }

    private ServerDTO createServerDTO() {
        return ServerDTO.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .createdAt(Instant.now())
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();
    }
}
