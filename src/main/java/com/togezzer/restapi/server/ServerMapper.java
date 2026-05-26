package com.togezzer.restapi.server;

import com.togezzer.restapi.server.dto.ServerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServerMapper {

    @Mapping(target = "isPublic", source = "public")
    ServerDTO toDto(ServerEntity entity);
}
