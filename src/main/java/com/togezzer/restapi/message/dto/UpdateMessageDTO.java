package com.togezzer.restapi.message.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMessageDTO extends MessagePostDTO{
    @Valid
    @NotNull(message = "Content is required")
    private ContentDTO content;
}
