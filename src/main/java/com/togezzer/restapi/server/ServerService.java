package com.togezzer.restapi.server;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.AlreadyInServerException;
import com.togezzer.restapi.exception.ServerNotFoundException;
import com.togezzer.restapi.exception.UserNotFoundException;
import com.togezzer.restapi.server.dto.JoinServerDTO;
import com.togezzer.restapi.server.dto.RenameServerDTO;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserId;
import com.togezzer.restapi.server_users.ServerUserRepository;
import com.togezzer.restapi.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final ServerUserRepository serverUserRepository;
    private final AuthUtils authUtils;

    public ServerService(ServerRepository serverRepository, UserRepository userRepository, ServerUserRepository serverUserRepository, AuthUtils authUtils){
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
        this.serverUserRepository = serverUserRepository;
        this.authUtils = authUtils;
    }

    private ServerEntity getServerByUuid(UUID serverUuid) {
        return serverRepository.findByUuid(serverUuid)
                .orElseThrow(() -> new ServerNotFoundException("Server with UUID " + serverUuid + " does not exist"));
    }

    public ServerDTO getServer(UUID serverUuid) {
        return this.entityToDto(getServerByUuid(serverUuid));
    }

    public ServerDTO createServer(final ServerDTO serverDTO) {

        final var serverEntityBuilder = ServerEntity.builder()
                .id(null)
                .uuid(serverDTO.getUuid())
                .name(serverDTO.getName())
                .createdAt(serverDTO.getCreatedAt())
                .createdBy(serverDTO.getCreatedBy())
                .isPublic(serverDTO.isPublic())
                .logo(serverDTO.getLogo())
                .background(serverDTO.getBackground());

        final var createdServerEntity = this.serverRepository.save(serverEntityBuilder.build());
        return this.entityToDto(createdServerEntity);
    }

    private ServerDTO entityToDto(final ServerEntity serverEntity) {
        return ServerDTO.builder()
                .id(serverEntity.getId())
                .uuid(serverEntity.getUuid())
                .name(serverEntity.getName())
                .createdAt(serverEntity.getCreatedAt())
                .createdBy(serverEntity.getCreatedBy())
                .isPublic(serverEntity.isPublic())
                .logo(serverEntity.getLogo())
                .background(serverEntity.getBackground())
                .build();
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
    }
}