package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotFoundRemoteException;
import com.togezzer.restapi.exception.RemoteApiClientException;
import com.togezzer.restapi.exception.RemoteApiServerException;
import com.togezzer.restapi.message.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class MessageApiClientService {
    private final RestClient restClient;

    @Autowired
    public MessageApiClientService(RestClient.Builder builder,
                                   @Value("${togezzer.chat-sauvegarde.base-url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public MessageDTO getMessageByRoomUuidAndMessageUuid(UUID roomUuid, UUID messageUuid) {
        return restClient.get()
                .uri("/api/messages/{roomId}/{messageUuid}", roomUuid, messageUuid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (request, response) -> handle4xxError(request.getURI().toString(), response.getStatusCode().value(), roomUuid, messageUuid)
                )
                .onStatus(HttpStatusCode::is5xxServerError,
                        (request, response) -> handle5xxError(request.getURI().toString(), response.getStatusCode().value())
                )
                .body(MessageDTO.class);
    }

    private void handle4xxError(String uri, int status, UUID roomUuid, UUID messageUuid) {
        if (status == 404) {
            throw new MessageNotFoundRemoteException(messageUuid, roomUuid);
        }

        throw new RemoteApiClientException(status, uri);
    }

    private void handle5xxError(String uri, int status) {
        throw new RemoteApiServerException(status, uri);
    }
}
