package com.togezzer.restapi.message;

import com.togezzer.restapi.message.service.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/messages/{roomUuid}/files", produces = MediaType.APPLICATION_JSON_VALUE)

@Validated
public class FileController {
    private final FileService fileService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadFile(@PathVariable UUID roomUuid,
                           @RequestPart("file") MultipartFile file){
        this.fileService.uploadFile(file, roomUuid);
    }

    @GetMapping("/{objectName}")
    public ResponseEntity<String> getFileUrl(
            @PathVariable UUID roomUuid,
            @PathVariable String objectName,
            @RequestParam UUID userUuid) {
        String url = fileService.getPresignedUrl(objectName, roomUuid, userUuid);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{objectName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(@PathVariable UUID roomUuid,
                           @PathVariable String objectName,
                           @RequestParam @NotNull UUID messageUuid) {
        this.fileService.deleteFile(objectName, roomUuid, messageUuid);
    }

}
