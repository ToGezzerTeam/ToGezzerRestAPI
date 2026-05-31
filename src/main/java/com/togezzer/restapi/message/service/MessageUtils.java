package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class MessageUtils {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final MessageApiClientService messageApiClientService;

    public void validateEntryExists(UUID roomUuid, UUID userUuid) {
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

    public MessageDTO createMessageDTO(UUID roomUuid, ContentDTO content, String answerTo, UUID authorUuid, String authorName, UUID messageUuid) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setContent(content);
        messageDTO.setAnswerTo(answerTo);
        messageDTO.setRoomId(roomUuid.toString());
        messageDTO.setState(MessageState.CREATED);
        messageDTO.setAuthorName(authorName);
        messageDTO.setCreatedAt(Instant.now());
        messageDTO.setUuid(messageUuid.toString());
        messageDTO.setAuthorId(authorUuid.toString());

        return messageDTO;
    }

    public MessageDTO applyMessageUpdate(MessageDTO messageDTO, String message){
        messageDTO.getContent().setValue(message);
        messageDTO.setState(MessageState.UPDATED);
        messageDTO.setUpdatedAt(Instant.now());

        return messageDTO;
    }

    public MessageDTO applyMessageDeletion(MessageDTO messageDTO, UUID userUuid){
        messageDTO.setState(MessageState.DELETED);
        messageDTO.setDeletedBy(userUuid.toString());
        messageDTO.setDeletedAt(Instant.now());
        messageDTO.getContent().setValue("");

        return messageDTO;
    }

    public MessageDTO getMessage(UUID roomUuid, UUID messageUuid) {
        return messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid);
    }

    public void isAuthorOfMessage(UUID userUuid, MessageDTO messageDTO) {
        if(!messageDTO.getAuthorId().equals(userUuid.toString())){
            throw new MessageNotOwnedByUserException(userUuid,messageDTO.getUuid());
        }
    }
}
