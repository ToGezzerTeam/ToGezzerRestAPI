package com.togezzer.restapi.room;

import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public interface RoomRepository extends ListCrudRepository<RoomEntity, Long> {
    Optional<RoomEntity> findByUuid(UUID uuid);
}
