package com.togezzer.restapi.server_users;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface ServerUserRepository extends ListCrudRepository<ServerUserEntity, Long> {
    boolean existsByServer_IdAndUser_Id(Long id, Long id1);
    boolean existsByServerUuidAndUserUuid(UUID serverId, UUID userId);
}
