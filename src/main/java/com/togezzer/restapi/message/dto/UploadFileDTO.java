package com.togezzer.restapi.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UploadFileDTO {
    @NotBlank
    private UUID authorId;
}
