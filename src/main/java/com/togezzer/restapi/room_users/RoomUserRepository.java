package com.togezzer.restapi.room_users;

import com.togezzer.restapi.room.RoomEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface RoomUserRepository extends ListCrudRepository<RoomUserEntity, Long> {
    boolean existsByRoom_IdAndUser_Id(Long id, Long id1);
    boolean existsByRoomUuidAndUserUuid(UUID roomId, UUID userId);
    void deleteAllByRoom(RoomEntity room);
}
