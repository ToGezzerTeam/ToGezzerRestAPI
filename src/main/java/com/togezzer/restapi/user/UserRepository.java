package com.togezzer.restapi.user;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Primary
@Repository
public interface UserRepository extends ListCrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuid(UUID s);
    boolean existsByUuid(UUID uuid);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<UserEntity> findAllByUuidIn(Set<UUID> authorIds);
}