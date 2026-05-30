package com.togezzer.restapi.server;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.AlreadyInServerException;
import com.togezzer.restapi.exception.ServerNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.room.RoomEntity;
import com.togezzer.restapi.room.RoomMapper;
import com.togezzer.restapi.room.RoomRepository;
import com.togezzer.restapi.room.RoomService;
import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.server.dto.DetailServerDto;
import com.togezzer.restapi.server.dto.JoinServerDTO;
import com.togezzer.restapi.server.dto.RenameServerDTO;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserId;
import com.togezzer.restapi.server_users.ServerUserRepository;
import com.togezzer.restapi.user.UserDto;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserMapper;
import com.togezzer.restapi.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final ServerUserRepository serverUserRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final UserMapper userMapper;
    private final ServerMapper serverMapper;
    private final AuthUtils authUtils;

    private ServerEntity getServerByUuid(UUID serverUuid) {
        return serverRepository.findByUuid(serverUuid)
                .orElseThrow(() -> new ServerNotFoundException("Server with UUID " + serverUuid + " does not exist"));
    }

    public ServerDTO getServer(UUID serverUuid) {
        return this.serverMapper.toDto(getServerByUuid(serverUuid));
    }

    public DetailServerDto getServerDetail(UUID serverUuid){
        ServerEntity serverEntity = getServerByUuid(serverUuid);
        List<RoomEntity> roomEntities = roomRepository.findByServer_Id(serverEntity.getId());
        List<UserEntity> userEntities = serverUserRepository.findAllByServer_Uuid(serverUuid)
                .stream()
                .map(ServerUserEntity::getUser)
                .toList();

        List<RoomDTO> roomDTOS = roomMapper.toDtoList(roomEntities);
        List<UserDto> userDtos = userMapper.toDtoList(userEntities);

        return new DetailServerDto(serverEntity.getId(), roomDTOS, userDtos);
    }

    public ServerDTO createServer(final ServerDTO serverDTO) {

        final var serverEntityBuilder = ServerEntity.builder()
                .id(null)
                .uuid(UUID.randomUUID())
                .name(serverDTO.getName())
                .createdAt(Instant.now())
                .createdBy(serverDTO.getCreatedBy())
                .isPublic(serverDTO.isPublic())
                .logo(serverDTO.getLogo())
                .background(serverDTO.getBackground());

        final var createdServerEntity = this.serverRepository.save(serverEntityBuilder.build());
        return this.serverMapper.toDto(createdServerEntity);
    }

    public void renameServer(UUID roomId, RenameServerDTO renameServerDTO) {
        final var serverEntity = getServerByUuid(roomId);

        serverEntity.setName(renameServerDTO.name());
        this.serverRepository.save(serverEntity);
    }


    public void join(final JoinServerDTO joinServerDTO, UUID serverUuid) {
        final var serverEntity = getServerByUuid(serverUuid);
        final UUID userUuid = authUtils.getCurrentUserUuid();

        final var userEntity = this.userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userUuid + " does not exist"));

        if (this.serverUserRepository.existsByServer_IdAndUser_Id(serverEntity.getId(), userEntity.getId())) {
            throw new AlreadyInServerException("User with ID " + userUuid + " is already in the server with ID " + joinServerDTO.getServerUuid());
        }

        final var serverUserId = new ServerUserId(serverEntity.getId(), userEntity.getId());
        final var serverUserEntity = ServerUserEntity.builder()
                .id(serverUserId)
                .server(serverEntity)
                .user(userEntity)
                .build();

        this.serverUserRepository.save(serverUserEntity);
        this.roomService.addUserToListRoom(userUuid, serverEntity.getId());
    }
}