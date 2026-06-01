package com.togezzer.restapi.auth;

import com.togezzer.restapi.auth.service.JwtService;
import com.togezzer.restapi.user.UserEntity;

import java.util.UUID;

public final class TestAuthTokenFactory {

    private TestAuthTokenFactory() {
    }

    public static String createBearerToken(JwtService jwtService) {
        return createBearerToken(jwtService, UUID.randomUUID());
    }

    public static String createBearerToken(JwtService jwtService, UUID userUuid) {
        UserEntity user = UserEntity.builder()
            .id(1L)
            .uuid(userUuid)
            .email("test@example.com")
            .username("testuser")
            .password("password")
            .build();

        return "Bearer " + jwtService.generateToken(user);
    }
}

