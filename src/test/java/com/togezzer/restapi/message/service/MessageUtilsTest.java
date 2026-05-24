package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageUtilsTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomUserRepository roomUserRepository;

    @Mock
    private MessageApiClientService messageApiClientService;

    private MessageUtils messageUtils;

    @BeforeEach
    void setUp() {
        messageUtils = new MessageUtils(roomRepository, userRepository, roomUserRepository, messageApiClientService);
    }

    @Test
    void validateEntryExists_whenAllValid_shouldNotThrow() {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(true);
        when(roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)).thenReturn(true);

        assertDoesNotThrow(() -> messageUtils.validateEntryExists(roomUuid, userUuid));
    }

    @Test
    void validateEntryExists_whenRoomMissing_shouldThrow() {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageUtils.validateEntryExists(roomUuid, userUuid)
        );

        assertEquals("Room with ID " + roomUuid + " does not exist", exception.getMessage());
    }

    @Test
    void validateEntryExists_whenUserMissing_shouldThrow() {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageUtils.validateEntryExists(roomUuid, userUuid)
        );

        assertEquals("User with ID " + userUuid + " does not exist", exception.getMessage());
    }

    @Test
    void validateEntryExists_whenUserNotInRoom_shouldThrow() {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(true);
        when(roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageUtils.validateEntryExists(roomUuid, userUuid)
        );

        assertEquals("User with ID " + userUuid + " is not in room with ID " + roomUuid, exception.getMessage());
    }

    @Test
    void buildMessageDTO_shouldFillAllFields() {
        UUID roomUuid = UUID.randomUUID();
        UUID authorUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        ContentDTO content = new ContentDTO();
        content.setType(ContentType.TEXT);
        content.setValue("hello");

        Instant before = Instant.now();

        MessageDTO messageDTO = messageUtils.createMessageDTO(
                roomUuid,
                content,
                null,
                authorUuid,
                messageUuid
        );

        Instant after = Instant.now();

        assertNotNull(messageDTO);
        assertEquals(content, messageDTO.getContent());
        assertNull(messageDTO.getAnswerTo());
        assertEquals(roomUuid.toString(), messageDTO.getRoomId());
        assertEquals(MessageState.CREATED, messageDTO.getState());
        assertNotNull(messageDTO.getCreatedAt());
        assertFalse(messageDTO.getCreatedAt().isBefore(before));
        assertFalse(messageDTO.getCreatedAt().isAfter(after));
        assertEquals(messageUuid.toString(), messageDTO.getUuid());
        assertEquals(authorUuid.toString(), messageDTO.getAuthorId());
    }

    @Test
    void buildMessageDTO_whenAnswerToIsSet_shouldPreserveIt() {
        UUID roomUuid = UUID.randomUUID();
        UUID authorUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        ContentDTO content = new ContentDTO();
        content.setType(ContentType.TEXT);
        content.setValue("hello");

        String answerTo = UUID.randomUUID().toString();

        MessageDTO messageDTO = messageUtils.createMessageDTO(
                roomUuid,
                content,
                answerTo,
                authorUuid,
                messageUuid
        );

        assertEquals(answerTo, messageDTO.getAnswerTo());
    }

    @Test
    void applyMessageUpdate_shouldUpdateContentStateAndTimestamp() {
        MessageDTO messageDTO = new MessageDTO();
        ContentDTO content = new ContentDTO();
        content.setValue("old message");
        messageDTO.setContent(content);

        Instant before = Instant.now();
        MessageDTO result = messageUtils.applyMessageUpdate(messageDTO, "new message");
        Instant after = Instant.now();

        assertEquals("new message", result.getContent().getValue());
        assertEquals(MessageState.UPDATED, result.getState());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getUpdatedAt().isBefore(before));
        assertFalse(result.getUpdatedAt().isAfter(after));
    }

    @Test
    void applyMessageDeletion_shouldSetDeletedStateAndMetadata() {
        UUID userUuid = UUID.randomUUID();
        MessageDTO messageDTO = new MessageDTO();
        ContentDTO contentDTO = ContentDTO.builder().type(ContentType.TEXT).value("test").build();
        messageDTO.setContent(contentDTO);

        Instant before = Instant.now();
        MessageDTO result = messageUtils.applyMessageDeletion(messageDTO, userUuid);
        Instant after = Instant.now();

        assertEquals(MessageState.DELETED, result.getState());
        assertEquals(userUuid.toString(), result.getDeletedBy());
        assertNotNull(result.getDeletedAt());
        assertFalse(result.getDeletedAt().isBefore(before));
        assertFalse(result.getDeletedAt().isAfter(after));
        assertEquals("",result.getContent().getValue());
    }

    @Test
    void getMessage_shouldDelegateToApiClient() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        MessageDTO expected = new MessageDTO();

        when(messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid)).thenReturn(expected);

        MessageDTO result = messageUtils.getMessage(roomUuid, messageUuid);

        assertEquals(expected, result);
    }

    @Test
    void isAuthorOfMessage_whenUserIsAuthor_shouldNotThrow() {
        UUID userUuid = UUID.randomUUID();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setAuthorId(userUuid.toString());
        messageDTO.setUuid(UUID.randomUUID().toString());

        assertDoesNotThrow(() -> messageUtils.isAuthorOfMessage(userUuid, messageDTO));
    }

    @Test
    void isAuthorOfMessage_whenUserIsNotAuthor_shouldThrow() {
        UUID userUuid = UUID.randomUUID();
        UUID authorUuid = UUID.randomUUID();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setAuthorId(authorUuid.toString());
        messageDTO.setUuid(UUID.randomUUID().toString());

        assertThrows(MessageNotOwnedByUserException.class,
                () -> messageUtils.isAuthorOfMessage(userUuid, messageDTO));
    }
}
