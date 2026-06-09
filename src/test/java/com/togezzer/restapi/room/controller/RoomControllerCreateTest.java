package com.togezzer.restapi.room.controller;

import com.togezzer.restapi.room.enums.ChannelType;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.room.messaging.RoomEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RoomEventProducer roomEventProducer;

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

    @Test
    void should_create_room_successfully() throws Exception {
        RoomDTO roomDTO = RoomDTO.builder()
                .name("General Chat")
                .channelType(ChannelType.TEXT)
                .build();

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void should_fail_when_channelType_is_null() throws Exception {
        RoomDTO invalidRoom = RoomDTO.builder()
                .channelType(null)
                .build();

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRoom)))
                .andExpect(status().isBadRequest());
    }
}
