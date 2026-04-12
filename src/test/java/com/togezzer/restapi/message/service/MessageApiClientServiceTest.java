package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotFoundRemoteException;
import com.togezzer.restapi.exception.RemoteApiClientException;
import com.togezzer.restapi.exception.RemoteApiServerException;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MessageApiClientServiceTest {

    private MockRestServiceServer server;
    private MessageApiClientService service;

    private final String baseUrl = "http://chat-sauvegarde";

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.createServer(restTemplate);

        RestClient.Builder builder = RestClient.builder(restTemplate);

        this.service = new MessageApiClientService(builder, baseUrl);
    }

    @Test
    void getMessage_returns_body_when_200() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        String json = "{" +
                "\"uuid\":\"" + messageUuid + "\"," +
                "\"roomId\":\"" + roomUuid + "\"," +
                "\"authorId\":\"" + UUID.randomUUID() + "\"," +
                "\"content\":{\"type\":\"TEXT\",\"value\":\"hello\"}," +
                "\"state\":\"CREATED\"," +
                "\"createdAt\":\"2025-01-01T00:00:00Z\"" +
                "}";

        this.server.expect(requestTo(baseUrl + "/api/messages/" + roomUuid + "/" + messageUuid))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        MessageDTO dto = service.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid);

        assertEquals(messageUuid.toString(), dto.getUuid());
        assertEquals(roomUuid.toString(), dto.getRoomId());
        assertNotNull(dto.getContent());
        assertEquals(ContentType.TEXT, dto.getContent().getType());
        assertEquals("hello", dto.getContent().getValue());
        assertEquals(MessageState.CREATED, dto.getState());
        assertEquals(Instant.parse("2025-01-01T00:00:00Z"), dto.getCreatedAt());

        this.server.verify();
    }

    @Test
    void getMessage_throws_MessageNotFoundRemoteException_on_404() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        this.server.expect(requestTo(baseUrl + "/api/messages/" + roomUuid + "/" + messageUuid))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(MessageNotFoundRemoteException.class,
                () -> service.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid));

        this.server.verify();
    }

    @Test
    void getMessage_throws_RemoteApiClientException_on_other_4xx() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        this.server.expect(requestTo(baseUrl + "/api/messages/" + roomUuid + "/" + messageUuid))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(RemoteApiClientException.class,
                () -> service.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid));

        this.server.verify();
    }

    @Test
    void getMessage_throws_RemoteApiServerException_on_5xx() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        this.server.expect(requestTo(baseUrl + "/api/messages/" + roomUuid + "/" + messageUuid))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(RemoteApiServerException.class,
                () -> service.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid));

        this.server.verify();
    }
}
