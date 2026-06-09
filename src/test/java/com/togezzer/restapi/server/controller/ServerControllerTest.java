package com.togezzer.restapi.server.controller;

import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import com.togezzer.restapi.room.enums.ChannelType;
import com.togezzer.restapi.room.RoomEntity;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.server.ServerEntity;
import com.togezzer.restapi.server.ServerRepository;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserId;
import com.togezzer.restapi.server_users.ServerUserRepository;
import com.togezzer.restapi.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.Instant;
import java.util.List;
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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private ServerUserRepository serverUserRepository;

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

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

        mockMvc.perform(post("/api/servers")
                        .header("Authorization", authHeader())
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

        mockMvc.perform(get("/api/servers/{serverUuid}", serverUuid.toString())
                        .header("Authorization", authHeader()))
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

        mockMvc.perform(get("/api/servers/{serverUuid}", serverUuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("does not exist")));
    }

    @Test
    void shouldGetServerDetailSuccessfully() throws Exception {
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
        doReturn(List.of()).when(roomRepository).findByServer_Id(serverEntity.getId());
        doReturn(List.of()).when(serverUserRepository).findAllByServer_Uuid(serverUuid);

        mockMvc.perform(get("/api/servers/{serverUuid}/detail", serverUuid)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serverId").value(1L))
                .andExpect(jsonPath("$.roomDTOS").isArray())
                .andExpect(jsonPath("$.userDtos").isArray());
    }

    @Test
    void shouldGetServerDetailWithRoomsAndUsers() throws Exception {
        final UUID serverUuid = UUID.randomUUID();
        final UUID roomUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();
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

        final var roomEntity = RoomEntity.builder()
                .id(1L)
                .uuid(roomUuid)
                .name("general")
                .channelType(ChannelType.TEXT)
                .createdAt(now)
                .server(serverEntity)
                .build();

        final var userEntity = UserEntity.builder()
                .id(1L)
                .uuid(userUuid)
                .username("alice")
                .email("alice@test.com")
                .password("password")
                .build();

        final var serverUserEntity = ServerUserEntity.builder()
                .id(new ServerUserId(serverEntity.getId(), userEntity.getId()))
                .server(serverEntity)
                .user(userEntity)
                .build();

        doReturn(Optional.of(serverEntity)).when(serverRepository).findByUuid(serverUuid);
        doReturn(List.of(roomEntity)).when(roomRepository).findByServer_Id(serverEntity.getId());
        doReturn(List.of(serverUserEntity)).when(serverUserRepository).findAllByServer_Uuid(serverUuid);

        mockMvc.perform(get("/api/servers/{serverUuid}/detail", serverUuid)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serverId").value(1L))
                .andExpect(jsonPath("$.roomDTOS", hasSize(1)))
                .andExpect(jsonPath("$.roomDTOS[0].name").value("general"))
                .andExpect(jsonPath("$.roomDTOS[0].serverId").value(1L))
                .andExpect(jsonPath("$.userDtos", hasSize(1)))
                .andExpect(jsonPath("$.userDtos[0].username").value("alice"))
                .andExpect(jsonPath("$.userDtos[0].uuid").value(userUuid.toString()));
    }

    @Test
    void shouldReturn404WhenGettingDetailOfUnknownServer() throws Exception {
        final var serverUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/servers/{serverUuid}/detail", serverUuid)
                        .header("Authorization", authHeader()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("does not exist")));
    }
}
