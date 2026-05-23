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

    @NotNull
    @NotBlank
    private String createdBy;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @JsonProperty("public")
    private boolean isPublic;

    @NotNull
    private String logo;

    @NotNull
    private String background;
}
