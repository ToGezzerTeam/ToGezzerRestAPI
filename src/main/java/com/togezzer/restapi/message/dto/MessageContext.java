package com.togezzer.restapi.message.dto;

import java.util.UUID;

public record MessageContext(UUID userUuid, MessageDTO messageDTO) {}
