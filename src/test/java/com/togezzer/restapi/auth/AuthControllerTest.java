package com.togezzer.restapi.auth;

import tools.jackson.databind.ObjectMapper;
import com.togezzer.restapi.auth.dto.LoginRequest;
import com.togezzer.restapi.auth.dto.RegisterRequest;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_returns_token_and_user() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setUsername("newuser");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("new@example.com"))
            .andExpect(jsonPath("$.user.username").value("newuser"));
    }

    @Test
    void register_when_user_exists_returns_409() throws Exception {
        userRepository.save(UserEntity.builder()
            .uuid(UUID.randomUUID())
            .email("existing@example.com")
            .username("existing")
            .password(passwordEncoder.encode("password123"))
            .build());

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setUsername("existing");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void login_returns_token_and_user() throws Exception {
        userRepository.save(UserEntity.builder()
            .uuid(UUID.randomUUID())
            .email("login@example.com")
            .username("loginuser")
            .password(passwordEncoder.encode("password123"))
            .build());

        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("login@example.com"))
            .andExpect(jsonPath("$.user.username").value("loginuser"));
    }

    @Test
    void login_when_password_invalid_returns_401() throws Exception {
        userRepository.save(UserEntity.builder()
            .uuid(UUID.randomUUID())
            .email("login-bad@example.com")
            .username("loginbad")
            .password(passwordEncoder.encode("password123"))
            .build());

        LoginRequest request = new LoginRequest();
        request.setEmail("login-bad@example.com");
        request.setPassword("wrong");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
