package com.togezzer.restapi.message.service;

import com.togezzer.restapi.config.MinioConfig;
import com.togezzer.restapi.exception.MinioException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.UploadFileDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private MessageUtils messageUtils;

    @Mock
    private MinioConfig minioConfig;

    @Mock
    private MessageEventProducer messageEventProducer;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileService fileService;

    private UUID roomUuid;
    private UUID authorId;
    private UploadFileDTO uploadFileDTO;

    @BeforeEach
    void setUp() {
        roomUuid = UUID.randomUUID();
        authorId = UUID.randomUUID();
        uploadFileDTO = new UploadFileDTO(authorId);
        lenient().when(minioConfig.minioClient()).thenReturn(minioClient);
    }

    @Test
    void uploadFile_shouldValidateEntryAndPublishMessage() throws Exception {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("photo.png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        MessageDTO builtMessage = mock(MessageDTO.class);
        when(messageUtils.buildMessageDTO(any(), any(), any(), any(), any())).thenReturn(builtMessage);

        // When
        fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid);

        // Then
        verify(messageUtils).validateEntryExists(roomUuid, authorId);
        verify(messageEventProducer).publishToQueues(builtMessage);
    }

    @Test
    void uploadFile_shouldStoreFileUrlInContent() throws Exception {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("doc.pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        ArgumentCaptor<ContentDTO> contentCaptor = ArgumentCaptor.forClass(ContentDTO.class);
        when(messageUtils.buildMessageDTO(any(), contentCaptor.capture(), any(), any(), any()))
                .thenReturn(mock(MessageDTO.class));

        // When
        fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid);

        // Then
        ContentDTO captured = contentCaptor.getValue();
        assertThat(captured.getType()).isEqualTo(ContentType.FILE);
        assertThat(captured.getValue())
                .startsWith("/api/messages/" + roomUuid + "/files/")
                .endsWith("_doc.pdf");
    }

    @Test
    void uploadFile_shouldCreateBucketWhenItDoesNotExist() throws Exception {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(messageUtils.buildMessageDTO(any(), any(), any(), any(), any()))
                .thenReturn(mock(MessageDTO.class));

        // When
        fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid);

        // Then
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void uploadFile_shouldNotCreateBucketWhenItAlreadyExists() throws Exception {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(messageUtils.buildMessageDTO(any(), any(), any(), any(), any()))
                .thenReturn(mock(MessageDTO.class));

        // When
        fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid);

        // Then
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void uploadFile_shouldThrowMinioException_whenBucketCheckFails() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("connection refused"));

        // When / Then
        assertThatThrownBy(() -> fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid))
                .isInstanceOf(MinioException.class)
                .hasMessageContaining("failed to check or create bucket")
                .hasMessageContaining(roomUuid.toString());
    }

    @Test
    void uploadFile_shouldThrowMinioException_whenPutObjectFails() throws Exception {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        doThrow(new RuntimeException("disk full"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        // When / Then
        assertThatThrownBy(() -> fileService.uploadFile(multipartFile, uploadFileDTO, roomUuid))
                .isInstanceOf(MinioException.class)
                .hasMessageContaining("upload failed")
                .hasMessageContaining(roomUuid.toString());
    }

    // -------------------------------------------------------------------------
    // getPresignedUrl
    // -------------------------------------------------------------------------

    @Test
    void getPresignedUrl_shouldReturnUrl() throws Exception {
        // Given
        UUID userUuid = UUID.randomUUID();
        String objectName = "abc_file.png";
        String expectedUrl = "http://minio/bucket/abc_file.png?X-Amz-Signature=xxx";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // When
        String result = fileService.getPresignedUrl(objectName, roomUuid, userUuid);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        verify(messageUtils).validateEntryExists(roomUuid, userUuid);
    }

    @Test
    void getPresignedUrl_shouldValidateUserBelongsToRoom() throws Exception {
        // Given
        UUID userUuid = UUID.randomUUID();
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio/presigned");

        // When
        fileService.getPresignedUrl("some_object", roomUuid, userUuid);

        // Then
        verify(messageUtils).validateEntryExists(roomUuid, userUuid);
    }

    @Test
    void getPresignedUrl_shouldThrowMinioException_whenMinioFails() throws Exception {
        // Given
        UUID userUuid = UUID.randomUUID();
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO unavailable"));

        // When / Then
        assertThatThrownBy(() -> fileService.getPresignedUrl("object.png", roomUuid, userUuid))
                .isInstanceOf(MinioException.class)
                .hasMessageContaining("failed to generate presigned URL")
                .hasMessageContaining(roomUuid.toString())
                .hasMessageContaining("object.png");
    }

    @Test
    void getPresignedUrl_shouldThrowWhenUserNotInRoom() {
        // Given
        UUID userUuid = UUID.randomUUID();
        doThrow(new RuntimeException("User not in room"))
                .when(messageUtils).validateEntryExists(roomUuid, userUuid);

        // When / Then
        assertThatThrownBy(() -> fileService.getPresignedUrl("object.png", roomUuid, userUuid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not in room");

        verifyNoInteractions(minioClient);
    }
}