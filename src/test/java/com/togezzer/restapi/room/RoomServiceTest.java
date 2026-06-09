package com.togezzer.restapi.room;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.ServerNotFoundException;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.room.dto.RoomEventDTO;
import com.togezzer.restapi.room.enums.ChannelType;
import com.togezzer.restapi.room.enums.StatusEvent;
import com.togezzer.restapi.room.messaging.RoomEventProducer;
import com.togezzer.restapi.room_users.RoomUserEntity;
import com.togezzer.restapi.room_users.RoomUserId;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.server.ServerEntity;
import com.togezzer.restapi.server.ServerRepository;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomUserRepository roomUserRepository;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private RoomEventProducer roomEventProducer;

    @Mock
    private ServerRepository serverRepository;

    @Spy
    private RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setup(){

        lenient().when(authUtils.getCurrentUserUuid()).thenReturn(userUuid);
        lenient().doNothing().when(roomEventProducer).publishToQueues(any(RoomEventDTO.class));
    }

    @Test
    void should_create_room_successfully() {
        // Arrange
        final var roomDto = RoomDTO.builder()
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .build();

        final var roomEntity = RoomEntity.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .createdAt(Instant.now())
                .server(null) // pas de server
                .build();

        doReturn(roomEntity).when(this.roomRepository).save(any(RoomEntity.class));
        doNothing().when(this.roomEventProducer).publishToQueues(any(RoomEventDTO.class));

        // Act
        assertDoesNotThrow(() -> roomService.create(roomDto));

        // Assert
        verify(this.roomRepository, times(1)).save(any(RoomEntity.class));
        verify(this.roomEventProducer, times(1)).publishToQueues(any(RoomEventDTO.class));
    }

    @Test
    void when_id_is_present_should_ignore_id() {
        // Arrange
        final var roomDto = RoomDTO.builder()
                .id(99L)
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .build();

        doReturn(this.createRoomEntity(UUID.randomUUID(), "Test room"))
                .when(this.roomRepository)
                .save(any(RoomEntity.class));

        // Act
        roomService.create(roomDto);

        // Assert
        final var argumentCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        verify(this.roomRepository).save(argumentCaptor.capture());

        assertNull(argumentCaptor.getValue().getId());
    }

    @Test
    void when_name_is_missing_should_use_uuid_as_name() {
        final var roomDto = RoomDTO.builder()
                .name(null)
                .channelType(ChannelType.VOICE)
                .build();

        final var uuid = UUID.randomUUID();

        final var argumentCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        doReturn(this.createRoomEntity(uuid, uuid.toString()))
                .when(this.roomRepository)
                .save(any(RoomEntity.class));

        roomService.create(roomDto);

        // Assert
        verify(this.roomRepository).save(argumentCaptor.capture());
        RoomEntity savedEntity = argumentCaptor.getValue();

        assertEquals(savedEntity.getUuid().toString(), savedEntity.getName());
    }

    @Test
    void when_name_is_blank_should_use_uuid_as_name() {
        final var roomDto = RoomDTO.builder()
                .name("  ")
                .channelType(ChannelType.VOICE)
                .build();

        final var uuid = UUID.randomUUID();

        final var argumentCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        doReturn(this.createRoomEntity(uuid, uuid.toString()))
                .when(this.roomRepository)
                .save(any(RoomEntity.class));

        roomService.create(roomDto);

        // Assert
        verify(this.roomRepository).save(argumentCaptor.capture());
        RoomEntity savedEntity = argumentCaptor.getValue();

        assertEquals(savedEntity.getUuid().toString(), savedEntity.getName());
    }

    @Test
    void when_room_id_does_not_exist_should_throw_RoomNotFoundException() {
        final var roomUuid = UUID.randomUUID();
        final var joinRoomDto = new JoinRoomDTO(roomUuid);

        when(this.roomRepository.findByUuid(roomUuid)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> this.roomService.join(joinRoomDto, roomUuid));
    }

    @Test
    void when_user_id_does_not_exist_should_throw_UserNotFoundException() {
        final var roomUuid = UUID.randomUUID();
        final var joinRoomDto = new JoinRoomDTO(roomUuid);

        when(this.roomRepository.findByUuid(roomUuid)).thenReturn(Optional.of(new RoomEntity()));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> this.roomService.join(joinRoomDto, roomUuid));
    }

    @Test
    void when_server_id_is_provided_should_link_server_to_room() {
        // Arrange
        final var serverId = 42L;
        final var serverEntity = ServerEntity.builder().id(serverId).build();

        final var roomDto = RoomDTO.builder()
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .serverId(serverId)
                .build();

        final var roomEntity = createRoomEntity(UUID.randomUUID(), "Test room");
        roomEntity.setServer(serverEntity);

        doReturn(Optional.of(serverEntity)).when(this.serverRepository).findById(serverId);
        doReturn(roomEntity).when(this.roomRepository).save(any(RoomEntity.class));

        // Act
        assertDoesNotThrow(() -> roomService.create(roomDto));

        // Assert
        final var argumentCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        verify(this.roomRepository).save(argumentCaptor.capture());

        assertNotNull(argumentCaptor.getValue().getServer());
        assertEquals(serverId, argumentCaptor.getValue().getServer().getId());
    }

    @Test
    void when_server_id_does_not_exist_should_throw_ServerNotFoundException() {
        // Arrange
        final var serverId = 99L;

        final var roomDto = RoomDTO.builder()
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .serverId(serverId)
                .build();

        doReturn(Optional.empty()).when(this.serverRepository).findById(serverId);

        // Act + Assert
        assertThrows(ServerNotFoundException.class, () -> roomService.create(roomDto));
        verify(this.roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    void should_rename_room_successfully() {
        // Arrange
        final var uuid = UUID.randomUUID();
        final var id = 10L;
        final var createdAt = Instant.parse("2025-01-01T10:00:00Z");

        final var existingEntity = RoomEntity.builder()
                .id(id)
                .uuid(UUID.fromString(uuid.toString()))
                .name("Old name")
                .channelType(ChannelType.TEXT)
                .createdAt(createdAt)
                .build();

        doReturn(Optional.of(existingEntity))
                .when(this.roomRepository)
                .findByUuid(uuid);
        doReturn(existingEntity).when(this.roomRepository).save(any(RoomEntity.class));

        final var request = new RenameRoomDTO("New name");

        // Act
        roomService.rename(uuid, request);

        // Assert
        final var argumentCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        verify(this.roomRepository).save(argumentCaptor.capture());

        final var saved = argumentCaptor.getValue();
        assertEquals(id, saved.getId());
        assertEquals(uuid, saved.getUuid());
        assertEquals("New name", saved.getName());
        assertEquals(ChannelType.TEXT, saved.getChannelType());
        assertEquals(createdAt, saved.getCreatedAt());

        verify(this.roomEventProducer, times(1)).publishToQueues(any(RoomEventDTO.class));
    }

    @Test
    void should_throw_when_renaming_unknown_room() {
        // Arrange
        final var uuid = UUID.randomUUID();
        doReturn(Optional.empty())
                .when(this.roomRepository)
                .findByUuid(uuid);

        // Act + Assert
        assertThrows(RoomNotFoundException.class, () -> roomService.rename(uuid, new RenameRoomDTO("New name")));
        verify(this.roomRepository, never()).save(any(RoomEntity.class));
    }

    @Test
    void when_user_already_in_room_should_throw_AlreadyInRoomException() {
        final var roomUuid = UUID.randomUUID();
        final var joinRoomDto = new JoinRoomDTO(roomUuid);
        final RoomEntity roomEntity = new RoomEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.roomRepository.findByUuid(roomUuid)).thenReturn(Optional.of(roomEntity));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.of(userEntity));
        when(this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())).thenReturn(true);

        assertThrows(AlreadyInRoomException.class, () -> this.roomService.join(joinRoomDto, roomUuid));
    }

    @Test
    void should_join_room_successfully(){
        final var roomUuid = UUID.randomUUID();
        final var joinRoomDto = new JoinRoomDTO(roomUuid);
        final RoomEntity roomEntity = new RoomEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.roomRepository.findByUuid(roomUuid)).thenReturn(Optional.of(roomEntity));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.of(userEntity));
        when(this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())).thenReturn(false);

        this.roomService.join(joinRoomDto, roomUuid);

        verify(this.roomUserRepository).save(any(RoomUserEntity.class));
    }


    @Test
    void addUserToListRoom_when_no_rooms_should_return_and_not_touch_user_or_saveAll() {
        // Arrange
        final var serverId = 123L;
        final var someUserUuid = UUID.randomUUID();

        when(roomRepository.findByServer_Id(serverId)).thenReturn(List.of());

        // Act
        roomService.addUserToListRoom(someUserUuid, serverId);

        // Assert
        verify(roomRepository).findByServer_Id(serverId);
        verifyNoInteractions(userRepository);
        verify(roomUserRepository, never()).saveAll(any());
    }

    @Test
    void should_add_user_to_all_rooms_of_server() {
        // Given
        long serverId = 123L;
        UUID userUuid = UUID.randomUUID();

        RoomEntity room1 = RoomEntity.builder().id(10L).build();
        RoomEntity room2 = RoomEntity.builder().id(11L).build();
        List<RoomEntity> rooms = List.of(room1, room2);

        UserEntity user = UserEntity.builder().id(99L).build();

        when(roomRepository.findByServer_Id(serverId)).thenReturn(rooms);
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));

        // When
        roomService.addUserToListRoom(userUuid, serverId);

        // Then
        ArgumentCaptor<List<RoomUserEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(roomUserRepository).saveAll(captor.capture());

        List<RoomUserEntity> result = captor.getValue();

        assertThat(result)
                .hasSize(2)
                .extracting(RoomUserEntity::getId)
                .containsExactlyInAnyOrder(
                        new RoomUserId(10L, 99L),
                        new RoomUserId(11L, 99L)
                );

        assertThat(result)
                .allSatisfy(roomUser -> {
                    assertThat(roomUser.getUser()).isSameAs(user);
                    assertThat(roomUser.getRoom()).isIn(rooms);
                });
    }

    @Test
    void addUserToListRoom_when_user_not_found_should_throw_and_not_saveAll() {
        // Arrange
        final var serverId = 123L;
        final var someUserUuid = UUID.randomUUID();

        final var r1 = RoomEntity.builder().id(10L).build();
        when(roomRepository.findByServer_Id(serverId)).thenReturn(List.of(r1));
        when(userRepository.findByUuid(someUserUuid)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UserNotFoundException.class, () -> roomService.addUserToListRoom(someUserUuid, serverId));
        verify(roomUserRepository, never()).saveAll(any());
    }

    @Test
    void should_delete_room_successfully() {
        // Arrange
        final var roomUuid = UUID.randomUUID();
        final var roomEntity = createRoomEntity(roomUuid, "Test room");

        doReturn(Optional.of(roomEntity)).when(this.roomRepository).findByUuid(roomUuid);

        // Act
        assertDoesNotThrow(() -> roomService.delete(roomUuid));

        // Assert
        verify(this.roomUserRepository, times(1)).deleteAllByRoom(roomEntity);
        verify(this.roomRepository, times(1)).delete(roomEntity);
        verify(this.roomEventProducer, times(1)).publishToQueues(any(RoomEventDTO.class));
    }

    @Test
    void should_delete_room_users_before_room() {
        // Arrange
        final var roomUuid = UUID.randomUUID();
        final var roomEntity = createRoomEntity(roomUuid, "Test room");

        doReturn(Optional.of(roomEntity)).when(this.roomRepository).findByUuid(roomUuid);

        // Act
        roomService.delete(roomUuid);

        // Assert — vérifie l'ordre : d'abord users, ensuite room
        final var inOrder = inOrder(roomUserRepository, roomRepository);
        inOrder.verify(roomUserRepository).deleteAllByRoom(roomEntity);
        inOrder.verify(roomRepository).delete(roomEntity);
    }

    @Test
    void should_throw_when_deleting_unknown_room() {
        // Arrange
        final var roomUuid = UUID.randomUUID();
        doReturn(Optional.empty()).when(this.roomRepository).findByUuid(roomUuid);

        // Act + Assert
        assertThrows(RoomNotFoundException.class, () -> roomService.delete(roomUuid));
        verify(this.roomUserRepository, never()).deleteAllByRoom(any());
        verify(this.roomRepository, never()).delete(any());
        verify(this.roomEventProducer, never()).publishToQueues(any());
    }

    @Test
    void should_delete_room_with_server_and_publish_event_with_server_id() {
        // Arrange
        final var roomUuid = UUID.randomUUID();
        final var serverUuid = UUID.randomUUID();
        final var serverEntity = ServerEntity.builder().id(42L).uuid(serverUuid).build();
        final var roomEntity = createRoomEntity(roomUuid, "Test room");
        roomEntity.setServer(serverEntity);

        doReturn(Optional.of(roomEntity)).when(this.roomRepository).findByUuid(roomUuid);

        final var eventCaptor = ArgumentCaptor.forClass(RoomEventDTO.class);

        // Act
        roomService.delete(roomUuid);

        // Assert
        verify(this.roomEventProducer).publishToQueues(eventCaptor.capture());
        assertEquals(StatusEvent.DELETED, eventCaptor.getValue().statusEvent());
        assertEquals(serverUuid, eventCaptor.getValue().serverUuid());
        assertEquals(roomUuid, eventCaptor.getValue().uuid());
    }

    @Test
    void should_delete_room_without_server_and_publish_event_with_null_server_id() {
        // Arrange
        final var roomUuid = UUID.randomUUID();
        final var roomEntity = createRoomEntity(roomUuid, "Test room"); // server = null

        doReturn(Optional.of(roomEntity)).when(this.roomRepository).findByUuid(roomUuid);

        final var eventCaptor = ArgumentCaptor.forClass(RoomEventDTO.class);

        // Act
        roomService.delete(roomUuid);

        // Assert
        verify(this.roomEventProducer).publishToQueues(eventCaptor.capture());
        assertEquals(StatusEvent.DELETED, eventCaptor.getValue().statusEvent());
        assertNull(eventCaptor.getValue().serverUuid());
    }

    private RoomEntity createRoomEntity(final UUID uuid, final String name) {
        return RoomEntity.builder()
                .id(1L)
                .uuid(uuid)
                .name(name)
                .channelType(ChannelType.TEXT)
                .createdAt(Instant.now())
                .server(null)
                .build();
    }
}