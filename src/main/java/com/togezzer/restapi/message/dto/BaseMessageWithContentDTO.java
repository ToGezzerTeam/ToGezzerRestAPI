package com.togezzer.restapi.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseMessageWithContentDTO extends MessagePostDTO {

    @NotBlank(message = "message is required")
    private String message;
}