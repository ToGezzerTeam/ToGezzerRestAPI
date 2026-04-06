package com.togezzer.restapi.message.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMessageDTO extends MessagePostDTO{
    @Valid
    @NotBlank(message = "Content is required")
    private ContentDTO content;
}
