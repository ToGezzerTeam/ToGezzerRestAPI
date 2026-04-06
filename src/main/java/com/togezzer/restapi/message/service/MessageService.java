package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MessageService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final MessageApiClientService messageApiClientService;
    private final MessageEventProducer messageEventProducer;

    public void updateMessage(UUID roomUuid, UUID messageUuid, UpdateMessageDTO updateMessageDTO) {
        validateEntryExists(roomUuid, updateMessageDTO.getUserUuid());
        MessageDTO messageDTO = getMessage(roomUuid, messageUuid);
        isAuthorOfMessage(updateMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(buildMessageDTO(messageDTO, updateMessageDTO));
    }

    public void deleteMessage(UUID roomUuid, UUID messageUuid, DeleteMessageDTO deleteMessageDTO) {
        validateEntryExists(roomUuid, deleteMessageDTO.getUserUuid());
        MessageDTO messageDTO = getMessage(roomUuid, messageUuid);
        isAuthorOfMessage(deleteMessageDTO.getUserUuid(), messageDTO);
        messageEventProducer.publishToQueues(buildMessageDTO(messageDTO, deleteMessageDTO));
    }

    private void validateEntryExists(UUID roomUuid, UUID userUuid) {
        validateRoomExists(roomUuid);
        validateUserExists(userUuid);
        validateUserInRoom(roomUuid, userUuid);
    }

    private void validateRoomExists(UUID roomUuid) {
        if(!roomRepository.existsByUuid(roomUuid)){
            throw new IllegalArgumentException("Room with ID " + roomUuid + " does not exist");
        }
    }

    private void validateUserExists(UUID userUuid) {
        if(!userRepository.existsByUuid(userUuid)){
            throw new IllegalArgumentException("User with ID " + userUuid + " does not exist");
        }
    }

    private void validateUserInRoom(UUID roomUuid, UUID userUuid) {
        if(!roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)){
            throw new IllegalArgumentException("User with ID " + userUuid + " is not in room with ID " + roomUuid);
        }
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
        messageDTO.setContent(updateMessageDTO.getContent());
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

}
