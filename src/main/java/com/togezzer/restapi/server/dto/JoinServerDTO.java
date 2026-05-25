package com.togezzer.restapi.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor

public class JoinServerDTO {

    private UUID serverUuid;
}

