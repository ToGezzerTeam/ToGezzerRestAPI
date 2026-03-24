package com.togezzer.restapi.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomRenameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void should_rename_room_successfully() throws Exception {
        // Create a room first so rename returns 200 (otherwise it returns 404)
        final var createRequest = RoomDTO.builder()
                .name("Old name")
                .channelType(ChannelType.TEXT)
                .build();

        final var createResponse = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var createdRoom = objectMapper.readValue(createResponse, RoomDTO.class);
        assertNotNull(createdRoom.getUuid());

        final var renameRequest = new RenameRoomDTO("New name");

        mockMvc.perform(patch("/api/rooms/{uuid}", createdRoom.getUuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_400_when_newName_is_blank() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameRoomDTO("   ");

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_body_is_missing() throws Exception {
        final var uuid = UUID.randomUUID();

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_404_when_room_does_not_exist() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameRoomDTO("New name");

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("n'existe pas")));
    }
}

