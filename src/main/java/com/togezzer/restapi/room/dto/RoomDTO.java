package com.togezzer.restapi.room.dto;

import com.togezzer.restapi.room.ChannelType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {
    private Long id;

    private UUID uuid;

    private String name;

    @NotNull(message = "channelType cannot be null")
    private ChannelType channelType;

    private Instant createdAt;

}
