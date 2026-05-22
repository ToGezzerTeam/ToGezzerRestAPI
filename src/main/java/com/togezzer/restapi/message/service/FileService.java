package com.togezzer.restapi.message.service;

import com.togezzer.restapi.config.MinioConfig;
import com.togezzer.restapi.message.dto.UploadFileDTO;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MessageUtils messageUtils;
    private final MinioConfig minioConfig;

    public void uploadFile(MultipartFile file, UploadFileDTO uploadFileDTO, UUID roomUuid){
        messageUtils.validateEntryExists(roomUuid,uploadFileDTO.getAuthorId());
        ensureBucketExists(roomUuid.toString());
        String url = getPresignedUrl(saveToMinio(file,roomUuid));

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
            throw new RuntimeException(e);
        }
    }


    private String saveToMinio(MultipartFile file,UUID roomUuid){
        try {
            String bucketName = roomUuid.toString();
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioConfig.minioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Erreur upload MinIO", e);
        }
    }


    public String getPresignedUrl(String objectName) {
        try {
            return minioConfig.minioClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("messages")
                            .object(objectName)
                            .expiry(Integer.MAX_VALUE) // 1h
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
