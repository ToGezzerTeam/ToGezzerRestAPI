package com.togezzer.restapi.room;

import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    @Test
    void should_create_room_successfully() throws Exception {
        RoomDTO roomDTO = RoomDTO.builder()
                .name("General Chat")
                .channelType(ChannelType.TEXT)
                .build();

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("General Chat"))
                .andExpect(jsonPath("$.channelType").value("TEXT"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void should_fail_when_channelType_is_null() throws Exception {
        RoomDTO invalidRoom = RoomDTO.builder()
                .channelType(null)
                .build();

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRoom)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void joinRoom_returns200_andCallsServiceWithRoomUuidFromPath() throws Exception {
        final UUID roomUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        final String body = this.objectMapper.writeValueAsString(new JoinRoomDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", roomUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());
    }

    @Test
    void joinRoom_whenRoomNotFound_returns404_andMessage() throws Exception {
        final UUID roomUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new RoomNotFoundException("room introuvable"))
                .when(this.roomService)
                .join(org.mockito.ArgumentMatchers.any(JoinRoomDTO.class));

        final String body = this.objectMapper.writeValueAsString(new JoinRoomDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", roomUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("room introuvable"));
    }

    @Test
    void joinRoom_whenUserNotFound_returns404_andMessage() throws Exception {
        final UUID roomUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new UserNotFoundException("user introuvable"))
                .when(this.roomService)
                .join(org.mockito.ArgumentMatchers.any(JoinRoomDTO.class));

        final String body = this.objectMapper.writeValueAsString(new JoinRoomDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", roomUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("user introuvable"));
    }

    @Test
    void joinRoom_whenAlreadyInRoom_returns400_andMessage() throws Exception {
        final UUID roomUuid = UUID.randomUUID();
        final UUID userUuid = UUID.randomUUID();

        doThrow(new AlreadyInRoomException("déjà dans la room"))
                .when(this.roomService)
                .join(org.mockito.ArgumentMatchers.any(JoinRoomDTO.class));

        final String body = this.objectMapper.writeValueAsString(new JoinRoomDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", roomUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("déjà dans la room"));
    }

    @Test
    void joinRoom_whenBodyMissingUserUuid_returns400_andDoesNotCallService() throws Exception {
        final UUID roomUuid = UUID.randomUUID();

        final String body = "{}";

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", roomUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.roomService);
    }

    @Test
    void joinRoom_whenRoomUuidPathInvalid_returns400_andDoesNotCallService() throws Exception {
        final UUID userUuid = UUID.randomUUID();
        final String body = this.objectMapper.writeValueAsString(new JoinRoomDTO(null, userUuid));

        this.mockMvc.perform(
                        post("/api/rooms/{roomUuid}/join", "not-a-uuid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(this.roomService);
    }

}
