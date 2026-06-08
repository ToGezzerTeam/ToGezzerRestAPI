package com.togezzer.restapi.server.dto;

import com.togezzer.restapi.room.dto.RoomDTO;
import com.togezzer.restapi.user.dto.UserDto;

import java.util.List;

public record DetailServerDto(
        Long serverId,
        List<RoomDTO> roomDTOS,
        List<UserDto> userDtos
) {
}
