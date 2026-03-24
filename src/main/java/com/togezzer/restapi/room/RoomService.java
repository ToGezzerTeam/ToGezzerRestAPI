package com.togezzer.restapi.room;

import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.room_users.RoomUserEntity;
import com.togezzer.restapi.room_users.RoomUserId;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, RoomUserRepository roomUserRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomUserRepository = roomUserRepository;
    }

    public RoomDTO create(final RoomDTO roomDTO) {
        final var uuid = UUID.randomUUID();

        final var roomEntityBuilder = RoomEntity.builder()
                .id(null)
                .uuid(uuid)
                .name(roomDTO.getName())
                .channelType(roomDTO.getChannelType())
                .createdAt(Instant.now());

        if (roomDTO.getName() == null || roomDTO.getName().isBlank()) {
            roomEntityBuilder.name(uuid.toString());
        }

        final var createdRoomEntity = this.roomRepository.save(roomEntityBuilder.build());
        return this.entityToDto(createdRoomEntity);
    }

    private RoomDTO entityToDto(final RoomEntity roomEntity) {
        return RoomDTO.builder()
                .id(roomEntity.getId())
                .uuid(roomEntity.getUuid())
                .name(roomEntity.getName())
                .channelType(roomEntity.getChannelType())
                .createdAt(roomEntity.getCreatedAt())
                .build();
    }

    public void join(final JoinRoomDTO joinRoomDTO) {
        final var roomEntity = this.roomRepository.findByUuid(joinRoomDTO.getRoomUuid())
                .orElseThrow(() -> new RoomNotFoundException("La room avec l'id " + joinRoomDTO.getRoomUuid() + " n'existe pas"));

        final var userEntity = this.userRepository.findByUuid(joinRoomDTO.getUserUuid())
                .orElseThrow(() -> new UserNotFoundException("L'utilisateur avec l'id " + joinRoomDTO.getUserUuid() + " n'existe pas"));

        if (this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())) {
            throw new AlreadyInRoomException("L'utilisateur avec l'id " + joinRoomDTO.getUserUuid() + " est déjà dans la room avec l'id " + joinRoomDTO.getRoomUuid());
        }

        final var roomUserId = new RoomUserId(roomEntity.getId(), userEntity.getId());
        final var roomUserEntity = RoomUserEntity.builder()
                .id(roomUserId)
                .room(roomEntity)
                .user(userEntity)
                .build();

        this.roomUserRepository.save(roomUserEntity);
    }
}
