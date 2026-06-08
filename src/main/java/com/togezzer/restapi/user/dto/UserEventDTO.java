package com.togezzer.restapi.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserEventDTO(
        UUID uuid,
        String userName,
        UUID serverUuid
){}
