package com.togezzer.restapi.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor

public class JoinServerDTO {

    private UUID serverUuid;

    @NotNull(message = "User's UUID is required.")
    private UUID userUuid;
}

