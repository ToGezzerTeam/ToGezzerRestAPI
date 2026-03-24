package com.togezzer.restapi.user;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public interface UserRepository extends ListCrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuid(UUID s);
}