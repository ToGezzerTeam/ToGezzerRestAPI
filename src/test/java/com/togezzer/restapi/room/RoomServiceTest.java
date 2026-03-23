package com.togezzer.restapi.room;

import com.togezzer.restapi.room.dto.RoomDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Test
    void should_create_room_successfully() {
        // Arrange
        final var roomDto = RoomDTO.builder()
                .name("Test room")
                .channelType(ChannelType.TEXT)
                .build();

        final String generatedUuid = UUID.randomUUID().toString();
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
        assertEquals(generatedUuid, created.getUuid().toString());
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

        assertEquals(savedEntity.getUuid(), savedEntity.getName());
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

        assertEquals(savedEntity.getUuid(), savedEntity.getName());
    }

    private RoomEntity createRoomEntity(final UUID uuid, final String name) {
        return RoomEntity.builder()
                .id(1L)
                .uuid(uuid.toString())
                .name(name)
                .channelType(ChannelType.TEXT)
                .createdAt(Instant.now())
                .build();
    }
}