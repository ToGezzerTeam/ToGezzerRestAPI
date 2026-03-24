package com.togezzer.restapi.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomRenameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_rename_room_successfully() throws Exception {
        final var uuid = UUID.randomUUID();
        final var request = new RenameRoomDTO("New name");

        mockMvc.perform(patch("/api/rooms/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("n'existe pas")));
    }
}

