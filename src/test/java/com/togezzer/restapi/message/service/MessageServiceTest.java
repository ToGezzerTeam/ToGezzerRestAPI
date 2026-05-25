package com.togezzer.restapi.message.service;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.MessageNotOwnedByUserException;
import com.togezzer.restapi.exception.MessagesPageNotFoundRemoteException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.CreateMessageDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.MessagesPageResponseDto;
import com.togezzer.restapi.message.dto.UpdateMessageDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.enums.MessageState;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoomUserRepository roomUserRepository;
    @Mock private MessageApiClientService messageApiClientService;
    @Mock private MessageEventProducer messageEventProducer;
    @Mock private MessageUtils messageUtils;
    @Mock private AuthUtils authUtils;

    @InjectMocks private MessageService messageService;

    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setup(){
        lenient().when(authUtils.getCurrentUserUuid()).thenReturn(userUuid);
    }

    @Test
    void updateMessage_should_publish_updated_message() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        UpdateMessageDTO update = new UpdateMessageDTO();
        update.setMessage("new");

        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(userUuid.toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("old").build())
                .state(MessageState.CREATED)
                .createdAt(createdAt)
                .build();

        doNothing().when(messageUtils).validateEntryExists(roomUuid, userUuid);
        doReturn(remote).when(messageUtils).getMessage(roomUuid, messageUuid);
        doNothing().when(messageUtils).isAuthorOfMessage(userUuid, remote);
        doAnswer(inv -> {
            MessageDTO dto = inv.getArgument(0);
            dto.getContent().setValue(inv.getArgument(1));
            dto.setState(MessageState.UPDATED);
            dto.setUpdatedAt(Instant.now());
            return dto;
        }).when(messageUtils).applyMessageUpdate(remote, "new");

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

        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(userUuid.toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("hello").build())
                .state(MessageState.CREATED)
                .createdAt(createdAt)
                .build();

        doNothing().when(messageUtils).validateEntryExists(roomUuid, userUuid);
        doReturn(remote).when(messageUtils).getMessage(roomUuid, messageUuid);
        doNothing().when(messageUtils).isAuthorOfMessage(userUuid, remote);
        doAnswer(inv -> {
            MessageDTO dto = inv.getArgument(0);
            dto.setState(MessageState.DELETED);
            dto.setDeletedBy(inv.getArgument(1).toString());
            dto.setDeletedAt(Instant.now());
            return dto;
        }).when(messageUtils).applyMessageDeletion(remote, userUuid);

        Instant before = Instant.now();
        messageService.deleteMessage(roomUuid, messageUuid);
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

        UpdateMessageDTO update = new UpdateMessageDTO();
        update.setMessage("x");

        doThrow(IllegalArgumentException.class).when(messageUtils).validateEntryExists(roomUuid,userUuid);

        assertThrows(IllegalArgumentException.class, () -> messageService.updateMessage(roomUuid, messageUuid, update));

        verifyNoInteractions(userRepository, roomUserRepository, messageApiClientService, messageEventProducer);
    }

    @Test
    void deleteMessage_when_not_author_should_throw_and_not_publish() {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        MessageDTO remote = MessageDTO.builder()
                .uuid(messageUuid.toString())
                .roomId(roomUuid.toString())
                .authorId(UUID.randomUUID().toString())
                .content(ContentDTO.builder().type(ContentType.TEXT).value("hello").build())
                .state(MessageState.CREATED)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        doNothing().when(messageUtils).validateEntryExists(roomUuid,userUuid);

        doReturn(remote).when(messageUtils).getMessage(roomUuid, messageUuid);
        doThrow(MessageNotOwnedByUserException.class).when(messageUtils).isAuthorOfMessage(userUuid,remote);

        assertThrows(MessageNotOwnedByUserException.class, () -> messageService.deleteMessage(roomUuid, messageUuid));

        verify(messageEventProducer, never()).publishToQueues(any());
    }

    @Test
    void createMessage_should_publish_created_message() {
        UUID roomUuid = UUID.randomUUID();

        CreateMessageDTO create = new CreateMessageDTO();
        create.setMessage("hello");
        create.setAnswerTo(null);

        doNothing().when(messageUtils).validateEntryExists(roomUuid,userUuid);
        doReturn(new MessageDTO()).when(messageUtils).createMessageDTO(eq(roomUuid),any(ContentDTO.class),eq(create.getAnswerTo()),eq(userUuid),any(UUID.class));

        messageService.createMessage(roomUuid, create);

        verify(messageEventProducer).publishToQueues(any(MessageDTO.class));

        verifyNoInteractions(messageApiClientService);
    }

    @Test
    void createMessage_when_validateEntryExists_throw_exception_and_not_publish() {
        UUID roomUuid = UUID.randomUUID();

        CreateMessageDTO create = new CreateMessageDTO();
        create.setMessage("hello");

        doThrow(IllegalArgumentException.class).when(messageUtils).validateEntryExists(roomUuid,userUuid);

        assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(roomUuid, create));

        verifyNoInteractions(userRepository, roomUserRepository, messageApiClientService, messageEventProducer);
    }

    @Test
    void getMessages_should_delegate_to_apiClient_and_return_result() {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String messageUuid = UUID.randomUUID().toString();
        int pageSize = 50;

        MessagesPageResponseDto expected = new MessagesPageResponseDto(List.of(), false);

        doNothing().when(messageUtils).validateEntryExists(roomUuid, userUuid);
        doReturn(expected).when(messageApiClientService).getMessagesByRoomId(roomUuid, messageUuid, pageSize);

        MessagesPageResponseDto result = messageService.getMessages(roomUuid, messageUuid, pageSize, userUuid);

        assertEquals(expected, result);
        verify(messageUtils).validateEntryExists(roomUuid, userUuid);
        verify(messageApiClientService).getMessagesByRoomId(roomUuid, messageUuid, pageSize);
    }

    @Test
    void getMessages_without_messageUuid_should_delegate_to_apiClient() {
        UUID roomUuid = UUID.randomUUID();
        int pageSize = 100;

        MessagesPageResponseDto expected = new MessagesPageResponseDto(List.of(), false);

        doNothing().when(messageUtils).validateEntryExists(roomUuid, userUuid);
        doReturn(expected).when(messageApiClientService).getMessagesByRoomId(roomUuid, null, pageSize);

        MessagesPageResponseDto result = messageService.getMessages(roomUuid, null, pageSize, userUuid);

        assertEquals(expected, result);
        verify(messageUtils).validateEntryExists(roomUuid, userUuid);
        verify(messageApiClientService).getMessagesByRoomId(roomUuid, null, pageSize);
    }

    @Test
    void getMessages_when_validateEntryExists_throws_should_not_call_apiClient() {
        UUID roomUuid = UUID.randomUUID();

        doThrow(IllegalArgumentException.class).when(messageUtils).validateEntryExists(roomUuid, userUuid);

        assertThrows(IllegalArgumentException.class,
                () -> messageService.getMessages(roomUuid, null, 100, userUuid));

        verifyNoInteractions(messageApiClientService, messageEventProducer);
    }

    @Test
    void getMessages_when_apiClient_throws_should_propagate_exception() {
        UUID roomUuid = UUID.randomUUID();

        doNothing().when(messageUtils).validateEntryExists(roomUuid, userUuid);
        doThrow(MessagesPageNotFoundRemoteException.class)
                .when(messageApiClientService).getMessagesByRoomId(roomUuid, null, 100);

        assertThrows(MessagesPageNotFoundRemoteException.class,
                () -> messageService.getMessages(roomUuid, null, 100, userUuid));

        verify(messageUtils).validateEntryExists(roomUuid, userUuid);
        verifyNoInteractions(messageEventProducer);
    }
}
