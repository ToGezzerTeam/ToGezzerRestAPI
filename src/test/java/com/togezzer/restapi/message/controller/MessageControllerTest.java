package com.togezzer.restapi.message.controller;

import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @Test
    void updateMessage_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        UpdateMessageDTO bodyDto = new UpdateMessageDTO();
        bodyDto.setUserUuid(userUuid);
        bodyDto.setContent(ContentDTO.builder().type(ContentType.TEXT).value("hi").build());

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        patch("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNoContent());

        verify(messageService).updateMessage(eq(roomUuid), eq(messageUuid), any(UpdateMessageDTO.class));
    }

    @Test
    void updateMessage_when_missing_userUuid_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        String body = "{}";

        mockMvc.perform(
                        patch("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void updateMessage_when_roomUuid_invalid_returns400_and_does_not_call_service() throws Exception {
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        UpdateMessageDTO bodyDto = new UpdateMessageDTO();
        bodyDto.setUserUuid(userUuid);
        bodyDto.setContent(ContentDTO.builder().type(ContentType.TEXT).value("hi").build());

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        patch("/api/messages/{roomUuid}/{messageUuid}", "not-a-uuid", messageUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void deleteMessage_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        DeleteMessageDTO bodyDto = new DeleteMessageDTO();
        bodyDto.setUserUuid(userUuid);

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNoContent());

        verify(messageService).deleteMessage(eq(roomUuid), eq(messageUuid), any(DeleteMessageDTO.class));
    }

    @Test
    void deleteMessage_when_missing_body_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }
}

