package com.togezzer.restapi.server_users;

import com.togezzer.restapi.server.dto.ServerDTO;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface ServerUserRepository extends ListCrudRepository<ServerUserEntity, Long> {
    boolean existsByServer_IdAndUser_Id(Long id, Long id1);
    boolean existsByServerUuidAndUserUuid(UUID serverId, UUID userId);
    List<ServerUserEntity> findAllByServer_Uuid(UUID serverUuid);

    List<ServerUserEntity> findAllByUser_Uuid(UUID userUuid);
}
