package com.togezzer.restapi.user;

import com.togezzer.restapi.server.dto.ServerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/api/users",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/servers")
    public List<ServerDTO> getServer() {
        return userService.getAllUserServers();
    }
}
