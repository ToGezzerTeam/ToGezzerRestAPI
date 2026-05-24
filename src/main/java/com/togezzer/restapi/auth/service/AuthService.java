package com.togezzer.restapi.auth.service;

import com.togezzer.restapi.auth.dto.JwtPayload;
import com.togezzer.restapi.auth.dto.LoginResponse;
import com.togezzer.restapi.auth.dto.UserResponse;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Authentifie un utilisateur via email et mot de passe bcrypt.
     * @param email email fourni
     * @param password mot de passe brut
     * @return reponse login avec token et user sans mot de passe
     */
    public LoginResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        JwtPayload payload = new JwtPayload(user.getId(), user.getUuid(), user.getEmail(), user.getUsername());
        UserResponse userResponse = new UserResponse(user.getId(), user.getUuid(), user.getEmail(), user.getUsername());

        return new LoginResponse(token, payload, userResponse);
    }

    /**
     * Inscrit un utilisateur, hash le mot de passe et retourne un JWT.
     * @param email email fourni
     * @param password mot de passe brut
     * @param username pseudo choisi
     * @return reponse login avec token et user sans mot de passe
     */
    public LoginResponse register(String email, String password, String username) {
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        UserEntity user = UserEntity.builder()
            .uuid(UUID.randomUUID())
            .email(email)
            .username(username)
            .password(passwordEncoder.encode(password))
            .build();

        UserEntity savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);
        JwtPayload payload = new JwtPayload(savedUser.getId(), savedUser.getUuid(), savedUser.getEmail(), savedUser.getUsername());
        UserResponse userResponse = new UserResponse(savedUser.getId(), savedUser.getUuid(), savedUser.getEmail(), savedUser.getUsername());

        return new LoginResponse(token, payload, userResponse);
    }
}
