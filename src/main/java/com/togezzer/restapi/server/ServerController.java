package com.togezzer.restapi.server;

import com.togezzer.restapi.server.dto.JoinServerDTO;
import com.togezzer.restapi.server.dto.RenameServerDTO;
import com.togezzer.restapi.server.dto.ServerDTO;
import jakarta.validation.Valid;
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

    @PostMapping("/{serverUuid}/join")
    @ResponseStatus(HttpStatus.OK)
    public void joinServer(@PathVariable @NotNull(message = "Server's UUID is required") UUID serverUuid, @Valid @RequestBody JoinServerDTO joinServerDTO) {
        this.serverService.join(joinServerDTO, serverUuid);
    }

    @PatchMapping("/{serverUuid}/rename")
    @ResponseStatus(HttpStatus.OK)
    public void renameServer(@PathVariable @NotNull(message = "Server's UUID is required") UUID serverUuid, @Valid @RequestBody RenameServerDTO request){
        this.serverService.renameServer(serverUuid, request);
    }
}