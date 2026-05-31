package com.togezzer.restapi.auth.service;

import com.togezzer.restapi.auth.dto.JwtPayload;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class AuthUtils {

    public JwtPayload getCurrentUser() {
        return (JwtPayload) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();
    }

    public UUID getCurrentUserUuid() {
        return getCurrentUser().getUuid();
    }

    public String getCurrentUserName(){ return getCurrentUser().getUsername(); }
}
