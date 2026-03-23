package com.togezzer.restapi.room_users;

import org.springframework.data.repository.ListCrudRepository;

public interface RoomUserRepository extends ListCrudRepository<RoomUserEntity, Long> {
    boolean existsByRoom_IdAndUser_Id(Long id, Long id1);
}
