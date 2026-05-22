package com.togezzer.restapi.server;

import com.togezzer.restapi.exception.ServerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTest {

    @InjectMocks
    ServerService serverService;

    @Mock
    ServerRepository serverRepository;

    @Test
    void shouldCreateServerSuccessfully(){

        final UUID generatedUuid = UUID.randomUUID();
        final Instant now = Instant.now();

        final var serverDTO = ServerDTO.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        final var serverEntity = ServerEntity.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        doReturn(serverEntity).when(this.serverRepository).save(any(ServerEntity.class));

        // Act
        final var created = serverService.createServer(serverDTO);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getCreatedBy()).isEqualTo("user");
        assertThat(created.getName()).isEqualTo("server");
        assertThat(created.isPublic()).isTrue();
        assertThat(created.getLogo()).isEqualTo("logo");
        assertThat(created.getBackground()).isEqualTo("blue");
    }

    @Test
    void shouldFindServerByUuid(){

        final UUID generatedUuid = UUID.randomUUID();
        final Instant now = Instant.now();

        final var serverEntity = ServerEntity.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        doReturn(Optional.of(serverEntity)).when(serverRepository).findByUuid(generatedUuid);

        ServerDTO serverDTO = serverService.getServer(generatedUuid);
        assertThat(serverDTO).isNotNull();
        assertThat(serverDTO.getId()).isEqualTo(1L);
        assertThat(serverDTO.getCreatedBy()).isEqualTo("user");
        assertThat(serverDTO.getName()).isEqualTo("server");
        assertThat(serverDTO.isPublic()).isTrue();
        assertThat(serverDTO.getLogo()).isEqualTo("logo");
        assertThat(serverDTO.getBackground()).isEqualTo("blue");
    }

    @Test
    void shouldThrowExceptionIfServerNotFound(){
        final UUID generatedUuid = UUID.randomUUID();
        final UUID uuidToFind = UUID.randomUUID();
        final Instant now = Instant.now();

        final var serverEntity = ServerEntity.builder()
                .id(1L)
                .uuid(generatedUuid)
                .createdAt(now)
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();

        assertThrows(ServerNotFoundException.class, () -> this.serverService.getServer(uuidToFind));
    }
}
