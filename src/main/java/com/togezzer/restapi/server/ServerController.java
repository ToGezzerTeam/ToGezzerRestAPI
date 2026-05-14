package com.togezzer.restapi.server;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/server", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServerController {
    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/{serverUuid}")
    public ServerDTO getServer(@PathVariable @NotNull UUID serverUuid){
        return serverService.getServer(serverUuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServerDTO createServer(@Valid @RequestBody ServerDTO serverDTO){
        return serverService.createServer(serverDTO);
    }
}

