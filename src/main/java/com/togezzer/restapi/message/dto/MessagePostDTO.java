package com.togezzer.restapi.message.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class MessagePostDTO {
    @NotNull(message = "UserUuid is required")
    private UUID userUuid;
}
