package com.togezzer.restapi.room.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class JoinRoomDTO {

        private UUID roomUuid;

        @NotBlank(message = "L'uuid de l'utilisateur est requis")
        private UUID userUuid;
}

