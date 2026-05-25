package com.togezzer.restapi.server;

import com.togezzer.restapi.auth.service.AuthUtils;
import com.togezzer.restapi.exception.*;
import com.togezzer.restapi.server.dto.JoinServerDTO;
import com.togezzer.restapi.server.dto.RenameServerDTO;
import com.togezzer.restapi.server.dto.ServerDTO;
import com.togezzer.restapi.server_users.ServerUserEntity;
import com.togezzer.restapi.server_users.ServerUserRepository;
import com.togezzer.restapi.user.UserEntity;
import com.togezzer.restapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTest {

    @InjectMocks
    ServerService serverService;

    @Mock
    UserRepository userRepository;

    @Mock
    ServerRepository serverRepository;

    @Mock
    ServerUserRepository serverUserRepository;

    @Mock
    AuthUtils authUtils;

    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setup(){
        lenient().when(authUtils.getCurrentUserUuid()).thenReturn(userUuid);
    }

    @Test
    void shouldCreateServerSuccessfully(){

        final var serverDTO = createServerDTO();
        final var serverEntity = createServerEntity();

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

        final var serverEntity = createServerEntity();

        doReturn(Optional.of(serverEntity)).when(serverRepository).findByUuid(serverEntity.getUuid());

        ServerDTO serverDTO = serverService.getServer(serverEntity.getUuid());
        assertThat(serverDTO).isNotNull();
        assertThat(serverDTO.getId()).isEqualTo(1L);
        assertThat(serverDTO.getCreatedBy()).isEqualTo("user");
        assertThat(serverDTO.getName()).isEqualTo("server");
        assertThat(serverDTO.isPublic()).isTrue();
        assertThat(serverDTO.getLogo()).isEqualTo("logo");
        assertThat(serverDTO.getBackground()).isEqualTo("blue");
    }

    @Test
    void whenIdServerIsPresentShouldIgnoreId() {

        final var serverDTO = createServerDTO();
        doReturn(this.createServerEntity())
                .when(this.serverRepository)
                .save(any(ServerEntity.class));

        // Act
        serverService.createServer(serverDTO);

        // Assert
        final var argumentCaptor = ArgumentCaptor.forClass(ServerEntity.class);
        verify(this.serverRepository).save(argumentCaptor.capture());

        assertNull(argumentCaptor.getValue().getId());
    }

    @Test
    void shouldThrowExceptionIfServerNotFound(){
        final UUID uuidToFind = UUID.randomUUID();

        when(this.serverRepository.findByUuid(uuidToFind)).thenReturn(Optional.empty());
        assertThrows(ServerNotFoundException.class, () -> this.serverService.getServer(uuidToFind));
    }

    @Test
    void should_join_server_successfully(){
        final var joinServerDTO = new JoinServerDTO(UUID.randomUUID());
        final var serverUuid = UUID.randomUUID();
        final ServerEntity serverEntity = new ServerEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.serverRepository.findByUuid(serverUuid)).thenReturn(Optional.of(serverEntity));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.of(userEntity));
        when(this.serverUserRepository.existsByServer_IdAndUser_Id(serverEntity.getId(), userEntity.getId())).thenReturn(false);

        this.serverService.join(joinServerDTO, serverUuid);

        verify(this.serverUserRepository).save(any(ServerUserEntity.class));
    }

    @Test
    void whenServerIdDoesNotExistShouldThrowServerNotFoundException() {
        final var joinServerDTO = new JoinServerDTO(UUID.randomUUID());
        final var serverUuid = UUID.randomUUID();

        when(this.serverRepository.findByUuid(serverUuid)).thenReturn(Optional.empty());

        assertThrows(ServerNotFoundException.class, () -> this.serverService.join(joinServerDTO, serverUuid));
    }

    @Test
    void whenUserIdDoesNotExistShouldThrowUserNotFoundExceptionwhen_user_id_does_not_exist_should_throw_UserNotFoundException() {
        final var joinServerDTO = new JoinServerDTO(UUID.randomUUID());
        final var serverUuid = UUID.randomUUID();

        when(this.serverRepository.findByUuid(serverUuid)).thenReturn(Optional.of(new ServerEntity()));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> this.serverService.join(joinServerDTO, serverUuid));
    }

    @Test
    void whenUserAlreadyInServerShouldThrowAlreadyInServerException() {
        final var joinServerDTO = new JoinServerDTO(UUID.randomUUID());
        final var serverUuid = UUID.randomUUID();
        final ServerEntity serverEntity = new ServerEntity();
        final UserEntity userEntity = new UserEntity();

        when(this.serverRepository.findByUuid(serverUuid)).thenReturn(Optional.of(serverEntity));
        when(this.userRepository.findByUuid(userUuid)).thenReturn(Optional.of(userEntity));
        when(this.serverUserRepository.existsByServer_IdAndUser_Id(serverEntity.getId(), userEntity.getId())).thenReturn(true);

        assertThrows(AlreadyInServerException.class, () -> this.serverService.join(joinServerDTO, serverUuid));
    }

    @Test
    void shouldRenameServerSuccessfully() {
        final var existingServerEntity = createServerEntity();

        doReturn(Optional.of(existingServerEntity))
                .when(this.serverRepository)
                .findByUuid(existingServerEntity.getUuid());
        doReturn(existingServerEntity).when(this.serverRepository).save(any(ServerEntity.class));

        final var request = new RenameServerDTO("New name");

        serverService.renameServer(existingServerEntity.getUuid(), request);

        final var argumentCaptor = ArgumentCaptor.forClass(ServerEntity.class);
        verify(this.serverRepository).save(argumentCaptor.capture());

        final var saved = argumentCaptor.getValue();
        assertEquals("New name", saved.getName());
    }

    @Test
    void shouldThrowExceptionWhenRenamingUnknownRoom() {
        // Arrange
        final var uuid = UUID.randomUUID();
        doReturn(Optional.empty())
                .when(this.serverRepository)
                .findByUuid(uuid);

        // Act + Assert
        assertThrows(ServerNotFoundException.class, () -> serverService.renameServer(uuid, new RenameServerDTO("New name")));
        verify(this.serverRepository, never()).save(any(ServerEntity.class));
    }

    private ServerEntity createServerEntity() {
        return ServerEntity.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .createdAt(Instant.now())
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();
    }

    private ServerDTO createServerDTO() {
        return ServerDTO.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .createdAt(Instant.now())
                .createdBy("user")
                .name("server")
                .isPublic(true)
                .logo("logo")
                .background("blue")
                .build();
    }
}
