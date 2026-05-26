package com.togezzer.restapi.room;

import com.togezzer.restapi.room.dto.RoomDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "serverId", source = "server.id")
    RoomDTO toDto(RoomEntity roomEntity);
    List<RoomDTO> toDtoList(List<RoomEntity> roomEntities);
}
