package com.togezzer.restapi.message.controller;

import com.togezzer.restapi.message.dto.CreateMessageDTO;
import com.togezzer.restapi.message.dto.MessagesPageResponseDto;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.service.MessageService;
import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private MessageService messageService;

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

    @Test
    void updateMessage_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        UpdateMessageDTO bodyDto = new UpdateMessageDTO();
        bodyDto.setMessage("hi");

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        patch("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .header("Authorization", authHeader())
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
                                .header("Authorization", authHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void updateMessage_when_roomUuid_invalid_returns400_and_does_not_call_service() throws Exception {
        UUID messageUuid = UUID.randomUUID();

        UpdateMessageDTO bodyDto = new UpdateMessageDTO();
        bodyDto.setMessage("hi");

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        patch("/api/messages/{roomUuid}/{messageUuid}", "not-a-uuid", messageUuid)
                                .header("Authorization", authHeader())
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

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/{messageUuid}", roomUuid, messageUuid)
                                .header("Authorization", authHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(messageService).deleteMessage(eq(roomUuid), eq(messageUuid));
    }

    @Test
    void createMessage_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();

        CreateMessageDTO bodyDto = new CreateMessageDTO();
        bodyDto.setMessage("hi");

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        post("/api/messages/{roomUuid}", roomUuid)
                                .header("Authorization", authHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNoContent());

        verify(messageService).createMessage(eq(roomUuid), any(CreateMessageDTO.class));
    }

    @Test
    void createMessage_when_missing_message_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        String body = "{\"userUuid\":\"" + userUuid + "\"}";

        mockMvc.perform(
                        post("/api/messages/{roomUuid}", roomUuid)
                                .header("Authorization", authHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void createMessage_when_roomUuid_invalid_returns400_and_does_not_call_service() throws Exception {
        CreateMessageDTO bodyDto = new CreateMessageDTO();
        bodyDto.setMessage("hi");

        String body = objectMapper.writeValueAsString(bodyDto);

        mockMvc.perform(
                        post("/api/messages/{roomUuid}", "not-a-uuid")
                                .header("Authorization", authHeader())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void getMessages_returns200_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        MessagesPageResponseDto response = new MessagesPageResponseDto(List.of(), false);

        doReturn(response).when(messageService).getMessages(eq(roomUuid), eq(messageUuid.toString()), eq(50), eq(userUuid));

        mockMvc.perform(
                        get("/api/messages/{roomUuid}", roomUuid)
                                .header("Authorization", authHeader())
                                .param("userUuid", userUuid.toString())
                                .param("lastMessageUuid", messageUuid.toString())
                                .param("pageSize", "50")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messageDTOS").isArray())
                .andExpect(jsonPath("$.hasMore").value(false));

        verify(messageService).getMessages(eq(roomUuid), eq(messageUuid.toString()), eq(50), eq(userUuid));
    }

    @Test
    void getMessages_without_messageUuid_uses_null_and_default_pageSize() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        MessagesPageResponseDto response = new MessagesPageResponseDto(List.of(), false);

        doReturn(response).when(messageService).getMessages(eq(roomUuid), isNull(), eq(100), eq(userUuid));

        mockMvc.perform(
                        get("/api/messages/{roomUuid}", roomUuid)
                                .header("Authorization", authHeader())
                                .param("userUuid", userUuid.toString())
                )
                .andExpect(status().isOk());

        verify(messageService).getMessages(eq(roomUuid), isNull(), eq(100), eq(userUuid));
    }

    @Test
    void getMessages_when_missing_userUuid_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/messages/{roomUuid}", roomUuid)
                        .header("Authorization", authHeader()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void getMessages_when_roomUuid_invalid_returns400_and_does_not_call_service() throws Exception {
        UUID userUuid = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/messages/{roomUuid}", "not-a-uuid")
                                .header("Authorization", authHeader())
                                .param("userUuid", userUuid.toString())
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void getMessages_when_pageSize_below_min_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/messages/{roomUuid}", roomUuid)
                                .header("Authorization", authHeader())
                                .param("userUuid", userUuid.toString())
                                .param("pageSize", "0")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }
}
