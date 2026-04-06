package com.togezzer.restapi.message.service;

import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.DeleteMessageDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoomUserRepository roomUserRepository;
    @Mock private MessageApiClientService messageApiClientService;
    @Mock private MessageEventProducer messageEventProducer;

    @InjectMocks private MessageService messageService;

    @Test
    void updateMessage_should_publish_updated_message() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        UpdateMessageDTO update = new UpdateMessageDTO();
        update.setUserUuid(userUuid);
        update.setContent(ContentDTO.builder().type(ContentType.TEXT).value("new").build());

        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(userUuid.toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("old").build())
                .state(MessageState.CREATED)
                .createdAt(createdAt)
                .build();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(true);
        when(roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)).thenReturn(true);
        when(messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid)).thenReturn(remote);

        Instant before = Instant.now();
        messageService.updateMessage(roomUuid, messageUuid, update);
        Instant after = Instant.now();

        ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(messageEventProducer).publishToQueues(captor.capture());

        MessageDTO published = captor.getValue();
        assertEquals(messageUuid.toString(), published.getUuid());
        assertEquals(roomUuid.toString(), published.getRoomId());
        assertEquals(userUuid.toString(), published.getAuthorId());
        assertEquals(MessageState.UPDATED, published.getState());
        assertNotNull(published.getUpdatedAt());
        assertFalse(published.getUpdatedAt().isBefore(before));
        assertFalse(published.getUpdatedAt().isAfter(after));
        assertEquals("new", published.getContent().getValue());
        assertEquals(ContentType.TEXT, published.getContent().getType());
        assertEquals(createdAt, published.getCreatedAt());
        assertNull(published.getDeletedAt());
        assertNull(published.getDeletedBy());
    }

    @Test
    void deleteMessage_should_publish_deleted_message() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        DeleteMessageDTO delete = new DeleteMessageDTO();
        delete.setUserUuid(userUuid);

        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(userUuid.toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("hello").build())
                .state(MessageState.CREATED)
                .createdAt(createdAt)
                .build();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(true);
        when(roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)).thenReturn(true);
        when(messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid)).thenReturn(remote);

        Instant before = Instant.now();
        messageService.deleteMessage(roomUuid, messageUuid, delete);
        Instant after = Instant.now();

        ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(messageEventProducer).publishToQueues(captor.capture());

        MessageDTO published = captor.getValue();
        assertEquals(MessageState.DELETED, published.getState());
        assertNotNull(published.getDeletedAt());
        assertFalse(published.getDeletedAt().isBefore(before));
        assertFalse(published.getDeletedAt().isAfter(after));
        assertEquals(userUuid.toString(), published.getDeletedBy());
        // delete ne doit pas modifier le contenu
        assertEquals("hello", published.getContent().getValue());
        assertNull(published.getUpdatedAt());
    }

    @Test
    void updateMessage_when_room_missing_should_throw_and_not_call_remote_nor_publish() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        UpdateMessageDTO update = new UpdateMessageDTO();
        update.setUserUuid(userUuid);
        update.setContent(ContentDTO.builder().type(ContentType.TEXT).value("x").build());

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> messageService.updateMessage(roomUuid, messageUuid, update));

        verifyNoInteractions(userRepository, roomUserRepository, messageApiClientService, messageEventProducer);
    }

    @Test
    void deleteMessage_when_not_author_should_throw_and_not_publish() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        DeleteMessageDTO delete = new DeleteMessageDTO();
        delete.setUserUuid(userUuid);

        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(UUID.randomUUID().toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("hello").build())
                .state(MessageState.CREATED)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        when(roomRepository.existsByUuid(roomUuid)).thenReturn(true);
        when(userRepository.existsByUuid(userUuid)).thenReturn(true);
        when(roomUserRepository.existsByRoomUuidAndUserUuid(roomUuid, userUuid)).thenReturn(true);
        when(messageApiClientService.getMessageByRoomUuidAndMessageUuid(roomUuid, messageUuid)).thenReturn(remote);

        assertThrows(MessageNotOwnedByUserException.class, () -> messageService.deleteMessage(roomUuid, messageUuid, delete));

        verify(messageEventProducer, never()).publishToQueues(any());
    }
}

