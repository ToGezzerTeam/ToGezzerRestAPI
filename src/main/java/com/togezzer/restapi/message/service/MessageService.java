package com.togezzer.restapi.message.service;

import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.CreateMessageDTO;
import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.MessagesPageResponseDto;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@AllArgsConstructor
public class MessageService {
    private final MessageEventProducer messageEventProducer;
    private final MessageUtils messageUtils;
    private final MessageApiClientService messageApiClientService;

    public void updateMessage(UUID roomUuid, UUID messageUuid, UpdateMessageDTO updateMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, updateMessageDTO.getUserUuid());
        MessageDTO messageDTO = messageUtils.getMessage(roomUuid, messageUuid);
        messageUtils.isAuthorOfMessage(updateMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(messageUtils.applyMessageUpdate(messageDTO, updateMessageDTO.getMessage()));
    }

    public void deleteMessage(UUID roomUuid, UUID messageUuid, DeleteMessageDTO deleteMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, deleteMessageDTO.getUserUuid());
        MessageDTO messageDTO = messageUtils.getMessage(roomUuid, messageUuid);
        messageUtils.isAuthorOfMessage(deleteMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(messageUtils.applyMessageDeletion(messageDTO, deleteMessageDTO.getUserUuid()));
    }


    public void createMessage(UUID roomUuid, CreateMessageDTO createMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, createMessageDTO.getUserUuid());
        UUID messageUuid = UUID.randomUUID();
        ContentDTO contentDTO = ContentDTO.builder().value(createMessageDTO.getMessage()).type(ContentType.TEXT).build();
        MessageDTO messageDTO = messageUtils.createMessageDTO(roomUuid,contentDTO, createMessageDTO.getAnswerTo(), createMessageDTO.getUserUuid(), messageUuid);
        messageEventProducer.publishToQueues(messageDTO);
    }

    public MessagesPageResponseDto getMessages(UUID roomUuid, String messageUuid, int pageSize, UUID userUuid) {
        messageUtils.validateEntryExists(roomUuid, userUuid);
        return messageApiClientService.getMessagesByRoomId(roomUuid, messageUuid, pageSize);
    }

}
