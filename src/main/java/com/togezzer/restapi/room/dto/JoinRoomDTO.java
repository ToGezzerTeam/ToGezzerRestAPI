package com.togezzer.restapi.room.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomDTO (
        @NotBlank(message = "L'id de la room est requis")
        String roomId,

        @NotBlank(message = "L'id de l'utilisateur est requis")
        String userId
){}

