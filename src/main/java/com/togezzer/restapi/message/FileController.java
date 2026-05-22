package com.togezzer.restapi.message;

import com.togezzer.restapi.message.dto.UploadFileDTO;
import com.togezzer.restapi.message.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/messages", produces = MediaType.APPLICATION_JSON_VALUE)


public class FileController {
    private final FileService fileService;

    @PostMapping("{roomUuid}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadFile(@PathVariable UUID roomUuid,
                           @RequestPart("data") @Valid UploadFileDTO uploadFileDTO,
                           @RequestPart("file") MultipartFile file){
        this.fileService.uploadFile(file, uploadFileDTO, roomUuid);
    }

}
