package com.togezzer.restapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private JwtPayload payload;
    private UserResponse user;
}

