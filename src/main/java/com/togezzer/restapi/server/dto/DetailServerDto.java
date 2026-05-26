package com.togezzer.restapi.server.dto;

import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.user.UserDto;

import java.util.List;

public record DetailServerDto(
        Long serverId,
        List<RoomDTO> roomDTOS,
        List<UserDto> userDtos
) {
}
