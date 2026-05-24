package com.togezzer.restapi.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerDTO {

    private Long id;

    private UUID uuid;

    private Instant createdAt;

    @NotNull(message = "creator's name can't be null")
    @NotBlank(message = "creator's name can't be blank")
    private String createdBy;

    @NotNull(message = "server's name can't be null")
    @NotBlank(message = "server's name can't be blank")
    private String name;

    @NotNull(message = "creator name can't be null")
    @JsonProperty("public")
    private boolean isPublic;

    @NotNull(message = "creator name can't be null")
    private String logo;

    @NotNull(message = "creator name can't be null")
    private String background;
}
