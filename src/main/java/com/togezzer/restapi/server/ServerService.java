package com.togezzer.restapi.server;

import com.togezzer.restapi.exception.ServerNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ServerService {

    private final ServerRepository serverRepository;

    public ServerService(ServerRepository serverRepository){
        this.serverRepository = serverRepository;
    }

    public ServerDTO getServer(UUID serverId) {
        return entityToDto(serverRepository.findByUuid(serverId)
                .orElseThrow(() -> new ServerNotFoundException("Server with ID " + serverId + " does not exist")));

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
}
