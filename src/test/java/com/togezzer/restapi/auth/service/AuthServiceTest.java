package com.togezzer.restapi.auth.service;

import com.togezzer.restapi.auth.dto.LoginResponse;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returns_token_and_user_payload() {
        UserEntity user = UserEntity.builder()
            .id(42L)
            .uuid(UUID.randomUUID())
            .email("user@example.com")
            .username("user")
            .password("hashed")
            .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token");

        LoginResponse response = authService.login("user@example.com", "password");

        assertEquals("token", response.getToken());
        assertNotNull(response.getPayload());
        assertNotNull(response.getUser());
        assertEquals(user.getEmail(), response.getUser().getEmail());
        assertEquals(user.getUsername(), response.getUser().getUsername());
    }

    @Test
    void login_when_password_invalid_returns_401() {
        UserEntity user = UserEntity.builder()
            .id(1L)
            .uuid(UUID.randomUUID())
            .email("user@example.com")
            .username("user")
            .password("hashed")
            .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login("user@example.com", "bad"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void register_when_user_exists_returns_409() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.register("user@example.com", "password", "user"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void register_returns_token_and_user_payload() {
        UserEntity saved = UserEntity.builder()
            .id(7L)
            .uuid(UUID.randomUUID())
            .email("new@example.com")
            .username("newuser")
            .password("hashed")
            .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("token");

        LoginResponse response = authService.register("new@example.com", "password", "newuser");

        assertEquals("token", response.getToken());
        assertEquals(saved.getEmail(), response.getUser().getEmail());
        assertEquals(saved.getUsername(), response.getUser().getUsername());
    }
}

