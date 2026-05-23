package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.CreateMessageDTO;
import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@AllArgsConstructor
public class MessageService {
    private final MessageApiClientService messageApiClientService;
    private final MessageEventProducer messageEventProducer;
    private final MessageUtils messageUtils;

    public void updateMessage(UUID roomUuid, UUID messageUuid, UpdateMessageDTO updateMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, updateMessageDTO.getUserUuid());
        MessageDTO messageDTO = getMessage(roomUuid, messageUuid);
        isAuthorOfMessage(updateMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(buildMessageDTO(messageDTO, updateMessageDTO));
    }

    public void deleteMessage(UUID roomUuid, UUID messageUuid, DeleteMessageDTO deleteMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, deleteMessageDTO.getUserUuid());
        MessageDTO messageDTO = getMessage(roomUuid, messageUuid);
        isAuthorOfMessage(deleteMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(buildMessageDTO(messageDTO, deleteMessageDTO));
    }


    private MessageDTO getMessage(UUID roomUuid, UUID messageUuid) {
        return messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid);
    }

    private void isAuthorOfMessage(UUID userUuid, MessageDTO messageDTO) {
        if(!messageDTO.getAuthorId().equals(userUuid.toString())){
            throw new MessageNotOwnedByUserException(userUuid,messageDTO.getUuid());
        }
    }

    private MessageDTO buildMessageDTO(MessageDTO messageDTO,UpdateMessageDTO updateMessageDTO){
        messageDTO.getContent().setValue(updateMessageDTO.getMessage());
        messageDTO.setState(MessageState.UPDATED);
        messageDTO.setUpdatedAt(Instant.now());

        return messageDTO;
    }

    private MessageDTO buildMessageDTO(MessageDTO messageDTO,DeleteMessageDTO deleteMessageDTO){
        messageDTO.setState(MessageState.DELETED);
        messageDTO.setDeletedBy(deleteMessageDTO.getUserUuid().toString());
        messageDTO.setDeletedAt(Instant.now());

        return messageDTO;
    }

    public void createMessage(UUID roomUuid, CreateMessageDTO createMessageDTO) {
        messageUtils.validateEntryExists(roomUuid, createMessageDTO.getUserUuid());
        UUID messageUuid = UUID.randomUUID();
        ContentDTO contentDTO = ContentDTO.builder().value(createMessageDTO.getMessage()).type(ContentType.TEXT).build();
        MessageDTO messageDTO = messageUtils.buildMessageDTO(roomUuid,contentDTO, createMessageDTO.getAnswerTo(), createMessageDTO.getUserUuid(), messageUuid);
        messageEventProducer.publishToQueues(messageDTO);
    }

}
