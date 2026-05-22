package com.togezzer.restapi.server;

import com.togezzer.restapi.room.dto.RenameRoomDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public class ServerControllerTest {

    @MockitoBean
    private ServerRepository serverRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateServerSuccessfully() throws Exception{

        final UUID generatedUuid = UUID.randomUUID();
        final Instant now = Instant.now();

        final var serverDTO = ServerDTO.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        final var serverEntity = ServerEntity.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        doReturn(serverEntity).when(this.serverRepository).save(any(ServerEntity.class));

        mockMvc.perform(post("/api/server")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serverDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.uuid").value(generatedUuid.toString()))
                .andExpect(jsonPath("$.createdAt").value(now.toString()))
                .andExpect(jsonPath("$.createdBy").value("user"))
                .andExpect(jsonPath("$.name").value("server"))
                .andExpect(jsonPath("$.public").value(true))
                .andExpect(jsonPath("$.logo").value("logo"))
                .andExpect(jsonPath("$.background").value("blue"));
    }

    @Test
    void shouldGetServerSuccessfully() throws Exception {

        final UUID serverUuid = UUID.randomUUID();
        final Instant now = Instant.now();

        final var serverEntity = ServerEntity.builder()
                .id(1L)
                .uuid(serverUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        doReturn(Optional.of(serverEntity)).when(serverRepository).findByUuid(serverUuid);

        mockMvc.perform(get("/api/server/{serverUuid}", serverUuid.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.uuid").value(serverUuid.toString()))
                .andExpect(jsonPath("$.createdAt").value(now.toString()))
                .andExpect(jsonPath("$.createdBy").value("user"))
                .andExpect(jsonPath("$.name").value("server"))
                .andExpect(jsonPath("$.public").value(true))
                .andExpect(jsonPath("$.logo").value("logo"))
                .andExpect(jsonPath("$.background").value("blue"));
    }

    @Test
    void shouldReturn404ErrorIfServerNotFound() throws Exception {
        final var serverUuid = UUID.randomUUID();
        final var request = new ServerDTO();

        mockMvc.perform(get("/api/server/{serverUuid}", serverUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("does not exist")));
    }
}
