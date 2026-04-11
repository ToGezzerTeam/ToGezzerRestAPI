package com.togezzer.restapi.room;

import com.togezzer.restapi.room.dto.RenameRoomDTO;
import com.togezzer.restapi.room.dto.JoinRoomDTO;
import com.togezzer.restapi.room.dto.RoomDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDTO createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return this.roomService.create(roomDTO);
    }

    @PatchMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public void renameRoom(@PathVariable @NotNull(message = "Room's UUID is required") UUID uuid, @RequestBody @Valid RenameRoomDTO request) {
        this.roomService.rename(uuid, request);
    }

    @PostMapping("{roomUuid}/join")
    @ResponseStatus(HttpStatus.OK)
    public void joinRoom(@PathVariable @NotNull(message = "Room's UUID is required") UUID roomUuid, @Valid @RequestBody JoinRoomDTO joinRoomDTO) {
        this.roomService.join(joinRoomDTO, roomUuid);
    }
}
