package com.togezzer.restapi.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameServerDTO(
        @NotBlank(message = "name should not be empty")
        @Size(max = 255, message = "name should not exceed 255 characters")
        String name
) {}
