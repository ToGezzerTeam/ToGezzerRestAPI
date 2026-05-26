package com.togezzer.restapi.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class JoinRoomDTO {

        private UUID roomUuid;
}

