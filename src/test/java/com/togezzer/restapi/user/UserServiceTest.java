package com.togezzer.restapi.user;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.server.ServerMapper;
import com.togezzer.restapi.server.ServerEntity;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private ServerUserRepository serverUserRepository;

    @Mock
    private AuthUtils authUtils;

    @Spy
    private ServerMapper serverMapper = Mappers.getMapper(ServerMapper.class);

    @Test
    void shouldReturnAllMappedServersForUser() {
        final var userUuid = UUID.randomUUID();
        final var now = Instant.now();
        final var firstServer = createServerEntity(1L, UUID.randomUUID(), "first-server", now);
        final var secondServer = createServerEntity(2L, UUID.randomUUID(), "second-server", now.plusSeconds(60));
        final var expectedFirst = serverMapper.toDto(firstServer);
        final var expectedSecond = serverMapper.toDto(secondServer);

        doReturn(userUuid).when(authUtils).getCurrentUserUuid();
        doReturn(List.of(
                ServerUserEntity.builder().server(firstServer).build(),
                ServerUserEntity.builder().server(secondServer).build()
        )).when(serverUserRepository).findAllByUser_Uuid(userUuid);

        final List<ServerDTO> result = userService.getAllUserServers();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst()).usingRecursiveComparison().isEqualTo(expectedFirst);
        assertThat(result.getLast()).usingRecursiveComparison().isEqualTo(expectedSecond);

        verify(serverUserRepository).findAllByUser_Uuid(userUuid);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoServers() {
        final var userUuid = UUID.randomUUID();

        doReturn(userUuid).when(authUtils).getCurrentUserUuid();
        doReturn(List.of()).when(serverUserRepository).findAllByUser_Uuid(userUuid);

        final List<ServerDTO> result = userService.getAllUserServers();

        assertThat(result).isEmpty();
        verify(serverUserRepository).findAllByUser_Uuid(userUuid);
    }

    private ServerEntity createServerEntity(final Long id, final UUID uuid, final String name, final Instant createdAt) {
        return ServerEntity.builder()
                .id(id)
                .uuid(uuid)
                .createdAt(createdAt)
                .createdBy("user")
                .name(name)
                .isPublic(id == 1L)
                .logo(name + "-logo")
                .background(name + "-background")
                .build();
    }
}


