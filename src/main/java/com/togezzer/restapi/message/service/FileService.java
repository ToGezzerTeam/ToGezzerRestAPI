package com.togezzer.restapi.message.service;

import com.togezzer.restapi.config.MinioConfig;
import com.togezzer.restapi.exception.MinioException;
import com.togezzer.restapi.message.dto.ContentDTO;
import com.togezzer.restapi.message.dto.MessageDTO;
import com.togezzer.restapi.message.dto.UploadFileDTO;
import com.togezzer.restapi.message.enums.ContentType;
import com.togezzer.restapi.message.messaging.MessageEventProducer;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MessageUtils messageUtils;
    private final MinioConfig minioConfig;
    private final MessageEventProducer messageEventProducer;

    public void uploadFile(MultipartFile file, UploadFileDTO uploadFileDTO, UUID roomUuid){
        messageUtils.validateEntryExists(roomUuid,uploadFileDTO.getAuthorId());
        ensureBucketExists(roomUuid.toString());
        UUID messageUuid = UUID.randomUUID();
        String sanitizedFilename = Objects.requireNonNull(file.getOriginalFilename())
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = messageUuid + "_" + sanitizedFilename;
        saveToMinio(file,roomUuid,objectName);
        String fileUrl = "/api/messages/" + roomUuid + "/files/" + objectName;
        ContentDTO contentDTO = ContentDTO.builder().value(fileUrl).type(ContentType.FILE).build();
        MessageDTO messageDTO = messageUtils.createMessageDTO(roomUuid, contentDTO, null, uploadFileDTO.getAuthorId(), roomUuid);
        messageEventProducer.publishToQueues(messageDTO);
    }


    private void ensureBucketExists(String bucketName) {
        try {
            boolean found = minioConfig.minioClient().bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!found) {
                minioConfig.minioClient().makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new MinioException("MinIO: failed to check or create bucket (bucket=" + bucketName + "): "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    private void saveToMinio(MultipartFile file, UUID roomUuid, String objectName) {
        String bucketName = roomUuid.toString();
        try {
            minioConfig.minioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new MinioException(
                    "MinIO: upload failed (bucket=" + bucketName + ", object=" + objectName + "): "
                            + e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }


    public String getPresignedUrl(String objectName, UUID roomUuid, UUID userUuid) {
        messageUtils.validateEntryExists(roomUuid, userUuid);
        String bucketName = roomUuid.toString();
        try {
            return minioConfig.minioClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60 * 60 * 24)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioException(
                    "MinIO: failed to generate presigned URL (bucket=" + bucketName + ", object=" + objectName + "): "
                            + e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    public void deleteFile(String objectName, UUID roomUuid, UUID userUuid, UUID messageUuid){
        messageUtils.validateEntryExists(roomUuid, userUuid);
        MessageDTO messageDTO = messageUtils.getMessage(roomUuid, messageUuid);
        messageUtils.isAuthorOfMessage(userUuid, messageDTO);
        deleteFromMinio(roomUuid, objectName);
        messageEventProducer.publishToQueues(messageUtils.applyMessageDeletion(messageDTO, messageUuid));
    }

    public void deleteFromMinio(UUID roomUuid, String objectName) {
        String bucketName = roomUuid.toString();
        try {
            minioConfig.minioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioException(
                    "MinIO: delete failed (bucket=" + bucketName + ", object=" + objectName + "): "
                            + e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }
}
