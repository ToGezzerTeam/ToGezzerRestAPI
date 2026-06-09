package com.togezzer.restapi.room.dto;

import com.togezzer.restapi.room.enums.ChannelType;
import com.togezzer.restapi.room.enums.StatusEvent;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RoomEventDTO(
        StatusEvent statusEvent,
        Long id,
        UUID uuid,
        String name,
        ChannelType channelType,
        UUID serverUuid
) {}
