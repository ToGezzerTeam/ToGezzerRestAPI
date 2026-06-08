package com.togezzer.restapi.room;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.ServerNotFoundException;
import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.exception.AlreadyInRoomException;
import com.togezzer.restapi.exception.RoomNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomEventDTO;
import com.togezzer.restapi.room.enums.StatusEvent;
import com.togezzer.restapi.room.messaging.RoomEventProducer;
import com.togezzer.restapi.room_users.RoomUserEntity;
import com.togezzer.restapi.room_users.RoomUserId;
import com.togezzer.restapi.room_users.RoomUserRepository;
import com.togezzer.restapi.server.ServerRepository;
import com.togezzer.restapi.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final ServerRepository serverRepository;
    private final AuthUtils authUtils;
    private final RoomEventProducer roomEventProducer;


    public void create(final RoomDTO roomDTO) {
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

        if (roomDTO.getServerId() != null) {
            final var serverEntity = this.serverRepository.findById(roomDTO.getServerId())
                    .orElseThrow(() -> new ServerNotFoundException("Server not found with id: " + roomDTO.getServerId()));
            roomEntityBuilder.server(serverEntity);
        }

        final var createdRoomEntity = this.roomRepository.save(roomEntityBuilder.build());
        final var roomEventDTO = RoomEventDTO.builder()
                .statusEvent(StatusEvent.CREATED)
                .id(createdRoomEntity.getId())
                .uuid(createdRoomEntity.getUuid())
                .name(createdRoomEntity.getName())
                .serverUuid(createdRoomEntity.getServer() != null ? createdRoomEntity.getServer().getUuid() : null)
                .build();

        roomEventProducer.publishToQueues(roomEventDTO);
    }


    private RoomEntity getRoomEntityByUUID(UUID roomId) {
        return this.roomRepository.findByUuid(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room with ID " + roomId + " does not exist"));
    }

    public void rename(UUID roomId, RenameRoomDTO renameRoomDTO) {
        final var roomEntity = getRoomEntityByUUID(roomId);

        roomEntity.setName(renameRoomDTO.name());
        final var renameRoomEntity = this.roomRepository.save(roomEntity);

        final var roomEventDTO = RoomEventDTO.builder()
                .statusEvent(StatusEvent.RENAME)
                .id(renameRoomEntity.getId())
                .uuid(renameRoomEntity.getUuid())
                .name(renameRoomEntity.getName())
                .serverUuid(renameRoomEntity.getServer() != null ? renameRoomEntity.getServer().getUuid() : null)
                .build();

        roomEventProducer.publishToQueues(roomEventDTO);
    }

    public void join(final JoinRoomDTO joinRoomDTO, final UUID roomUuid) {
        final var roomEntity = getRoomEntityByUUID(roomUuid);
        final UUID userUuid = authUtils.getCurrentUserUuid();

        final var userEntity = this.userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userUuid + " does not exist"));

        if (this.roomUserRepository.existsByRoom_IdAndUser_Id(roomEntity.getId(), userEntity.getId())) {
            throw new AlreadyInRoomException("User with ID " + userUuid + " is already in the room with ID " + joinRoomDTO.getRoomUuid());
        }

        final var roomUserId = new RoomUserId(roomEntity.getId(), userEntity.getId());
        final var roomUserEntity = RoomUserEntity.builder()
                .id(roomUserId)
                .room(roomEntity)
                .user(userEntity)
                .build();

        this.roomUserRepository.save(roomUserEntity);
    }

    public void addUserToListRoom(UUID userUuid, Long serverId){
        List<RoomEntity> roomEntities = roomRepository.findByServer_Id(serverId);
        if(roomEntities.isEmpty()){
          return;
        }

        final var userEntity = this.userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userUuid + " does not exist"));

        final var roomUserEntities = roomEntities.stream()
                .map(roomEntity -> RoomUserEntity.builder()
                        .id(new RoomUserId(roomEntity.getId(), userEntity.getId()))
                        .room(roomEntity)
                        .user(userEntity)
                        .build())
                .toList();

        this.roomUserRepository.saveAll(roomUserEntities);
    }

    public void delete(UUID roomUuid){
        final var roomEntity = getRoomEntityByUUID(roomUuid);
        roomUserRepository.deleteAllByRoom(roomEntity);
        roomRepository.delete(roomEntity);

        final var roomEventDTO = RoomEventDTO.builder()
                .statusEvent(StatusEvent.DELETED)
                .id(roomEntity.getId())
                .uuid(roomEntity.getUuid())
                .name(roomEntity.getName())
                .serverUuid(roomEntity.getServer() != null ? roomEntity.getServer().getUuid() : null)
                .build();

        roomEventProducer.publishToQueues(roomEventDTO);
    }
}
