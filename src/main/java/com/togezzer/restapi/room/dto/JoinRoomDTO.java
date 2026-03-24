package com.togezzer.restapi.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class JoinRoomDTO {

        private UUID roomUuid;

        @NotNull(message = "User's UUID is required.")
        private UUID userUuid;
}

