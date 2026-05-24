package com.togezzer.restapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JwtPayload {
    private Long id;
    private UUID uuid;
    private String email;
    private String username;
}

