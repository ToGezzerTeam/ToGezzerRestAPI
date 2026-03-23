package com.togezzer.restapi.room;

import com.togezzer.restapi.room.dto.RoomDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomDTO create(final RoomDTO roomDTO) {
        final var uuid =UUID.randomUUID().toString();

        final var roomEntityBuilder = RoomEntity.builder()
                .id(null)
                .uuid(uuid)
                .name(roomDTO.getName())
                .channelType(roomDTO.getChannelType())
                .createdAt(Instant.now());

        if (roomDTO.getName() == null || roomDTO.getName().isBlank()) {
            roomEntityBuilder.name(uuid);
        }

        final var createdRoomEntity = this.roomRepository.save(roomEntityBuilder.build());
        return this.entityToDto(createdRoomEntity);
    }

    private RoomDTO entityToDto(final RoomEntity roomEntity) {
        return RoomDTO.builder()
                .id(roomEntity.getId())
                .uuid(UUID.fromString(roomEntity.getUuid()))
                .name(roomEntity.getName())
                .channelType(roomEntity.getChannelType())
                .createdAt(roomEntity.getCreatedAt())
                .build();
    }
}
