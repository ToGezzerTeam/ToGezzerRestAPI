package com.togezzer.restapi.room;

import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.room.enums.ChannelType;
import com.togezzer.restapi.room.messaging.RoomEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomRenameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RoomEventProducer roomEventProducer;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

    @Test
    void should_rename_room_successfully() throws Exception {
        // Crée la room directement en base
        final var roomEntity = roomRepository.save(RoomEntity.builder()
                .uuid(UUID.randomUUID())
                .name("Old name")
                .channelType(ChannelType.TEXT)
                .createdAt(Instant.now())
                .build());

        final var renameRequest = new RenameRoomDTO("New name");

        mockMvc.perform(patch("/api/rooms/{uuid}", roomEntity.getUuid())
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_400_when_newName_is_blank() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameRoomDTO("   ");

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_body_is_missing() throws Exception {
        final var uuid = UUID.randomUUID();

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_404_when_room_does_not_exist() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameRoomDTO("New name");

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("does not exist")));
    }
}
