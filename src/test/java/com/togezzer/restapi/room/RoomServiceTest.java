package com.togezzer.restapi.room;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.room_users.RoomUserEntity;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomUserRepository roomUserRepository;

    @Test
    void should_create_room_successfully() {
        // Arrange
        final var roomDto = RoomDTO.builder()
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .build();

        final UUID generatedUuid = UUID.randomUUID();
        final Instant now = Instant.now();

        final var roomEntity = RoomEntity.builder()
                .id(1L)
                .uuid(generatedUuid)
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .createdAt(now)
                .build();

        doReturn(roomEntity).when(this.roomRepository).save(any(RoomEntity.class));

        // Act
        final var created = roomService.create(roomDto);

        // Assert
        assertEquals(1L, created.getId());
        assertEquals(generatedUuid, created.getUuid());
        assertEquals("Test room", created.getName());
        assertEquals(ChannelType.TEXT, created.getChannelType());
        assertEquals(now, created.getCreatedAt());
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
        final var joinRoomDto = new JoinRoomDTO(UUID.randomUUID(), UUID.randomUUID());

        when(this.roomRepository.findByUuid(joinRoomDto.getRoomUuid())).thenReturn(java.util.Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> this.roomService.join(joinRoomDto));
    }

    @Test
    void when_user_id_does_not_exist_should_throw_UserNotFoundException() {
        final var joinRoomDto = new JoinRoomDTO(UUID.randomUUID(), UUID.randomUUID());

        when(this.roomRepository.findByUuid(joinRoomDto.getRoomUuid())).thenReturn(Optional.of(new RoomEntity()));
        when(this.userRepository.findByUuid(joinRoomDto.getUserUuid())).thenReturn(java.util.Optional.empty());

        assertThrows(UserNotFoundException.class, () -> this.roomService.join(joinRoomDto));
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

        doReturn(java.util.Optional.of(existingEntity))
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
        assertEquals(uuid.toString(), saved.getUuid());
        assertEquals("New name", saved.getName());
        assertEquals(ChannelType.TEXT, saved.getChannelType());
        assertEquals(createdAt, saved.getCreatedAt());
    }

    @Test
    void should_throw_when_renaming_unknown_room() {
        // Arrange
        final var uuid = UUID.randomUUID();
        doReturn(java.util.Optional.empty())
                .when(this.roomRepository)
                .findByUuid(uuid);

        // Act + Assert
        assertThrows(RoomNotFoundException.class, () -> roomService.rename(uuid, new RenameRoomDTO("New name")));
        verify(this.roomRepository, never()).save(any(RoomEntity.class));
    }

    void when_user_already_in_room_should_throw_AlreadyInRoomException() {
        final var joinRoomDto = new JoinRoomDTO(UUID.randomUUID(), UUID.randomUUID());
        final RoomEntity roomEntity = new RoomEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.roomRepository.findByUuid(joinRoomDto.getRoomUuid())).thenReturn(Optional.of(roomEntity));
        when(this.userRepository.findByUuid(joinRoomDto.getUserUuid())).thenReturn(Optional.of(userEntity));
        when(this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())).thenReturn(true);

        assertThrows(AlreadyInRoomException.class, () -> this.roomService.join(joinRoomDto));
    }

    @Test
    void should_join_room_successfully(){
        final var joinRoomDto = new JoinRoomDTO(UUID.randomUUID(), UUID.randomUUID());
        final RoomEntity roomEntity = new RoomEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.roomRepository.findByUuid(joinRoomDto.getRoomUuid())).thenReturn(Optional.of(roomEntity));
        when(this.userRepository.findByUuid(joinRoomDto.getUserUuid())).thenReturn(Optional.of(userEntity));
        when(this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())).thenReturn(false);

        this.roomService.join(joinRoomDto);

        verify(this.roomUserRepository).save(any(RoomUserEntity.class));
    }

    private RoomEntity createRoomEntity(final UUID uuid, final String name) {
        return RoomEntity.builder()
                .id(1L)
                .uuid(uuid)
                .name(name)
                .channelType(ChannelType.TEXT)
                .createdAt(Instant.now())
                .build();
    }
}