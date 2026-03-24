package com.togezzer.restapi.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameRoomDTO(
		@NotBlank(message = "newName ne doit pas être vide")
		@Size(max = 100, message = "newName doit faire au maximum 100 caractères")
		String newName
) {}
