package com.togezzer.restapi.user;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.server.ServerMapper;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final ServerUserRepository serverUserRepository;

    private final ServerMapper serverMapper;

    private final  AuthUtils authUtils;

    public List<ServerDTO> getAllUserServers(){
        final var userUuid = authUtils.getCurrentUserUuid();
        return serverUserRepository.findAllByUser_Uuid(userUuid).stream()
                .map(ServerUserEntity::getServer)
                .map(serverMapper::toDto)
                .toList();
    }
}
