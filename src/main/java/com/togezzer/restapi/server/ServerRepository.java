package com.togezzer.restapi.server;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public interface ServerRepository extends ListCrudRepository<ServerEntity, Long> {
    Optional<ServerEntity> findByUuid(UUID uuid);
    boolean existsByUuid(UUID uuid);
}