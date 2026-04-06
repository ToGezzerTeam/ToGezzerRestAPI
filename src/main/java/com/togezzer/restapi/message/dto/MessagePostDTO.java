package com.togezzer.restapi.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class MessagePostDTO {
    @NotBlank(message = "UserUuid is required")
    private UUID userUuid;
}
