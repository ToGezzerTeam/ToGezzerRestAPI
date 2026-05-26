package com.togezzer.restapi.auth.service;

import com.togezzer.restapi.auth.dto.JwtPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsJwtPayloadPrincipal() {
        JwtPayload payload = mock(JwtPayload.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(payload);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthUtils authUtils = new AuthUtils();

        JwtPayload result = authUtils.getCurrentUser();

        assertSame(payload, result);
    }

    @Test
    void getCurrentUser_throwsNullPointerException_whenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(securityContext);

        AuthUtils authUtils = new AuthUtils();

        assertThrows(NullPointerException.class, authUtils::getCurrentUser);
    }

    @Test
    void getCurrentUser_throwsClassCastException_whenPrincipalIsNotJwtPayload() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt-payload");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthUtils authUtils = new AuthUtils();

        assertThrows(ClassCastException.class, authUtils::getCurrentUser);
    }

    @Test
    void getCurrentUserUuid_returnsUuid_fromCurrentUser() {
        UUID uuid = UUID.randomUUID();

        JwtPayload payload = mock(JwtPayload.class);
        when(payload.getUuid()).thenReturn(uuid);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(payload);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthUtils authUtils = new AuthUtils();

        UUID result = authUtils.getCurrentUserUuid();

        assertEquals(uuid, result);
        verify(payload, times(1)).getUuid();
    }
}
