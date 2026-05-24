package com.togezzer.restapi.message.controller;

import com.togezzer.restapi.message.dto.UploadFileDTO;
import com.togezzer.restapi.message.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    void uploadFile_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID authorUuid = UUID.randomUUID();

        String dataJson = "{\"authorId\":\"" + authorUuid + "\"}";

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                dataJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/messages/{roomUuid}/files", roomUuid)
                                .file(dataPart)
                                .file(filePart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isNoContent());

        verify(fileService).uploadFile(any(MultipartFile.class), any(UploadFileDTO.class), eq(roomUuid));
    }

    @Test
    void uploadFile_when_missing_data_part_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/messages/{roomUuid}/files", roomUuid)
                                .file(filePart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileService);
    }

    @Test
    void getFileUrl_returns200_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String objectName = "file.txt";

        String expectedUrl = "http://minio/presigned-url";

        org.mockito.Mockito.when(fileService.getPresignedUrl(objectName, roomUuid, userUuid)).thenReturn(expectedUrl);

        mockMvc.perform(
                        get("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .param("userUuid", userUuid.toString())
                )
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));

        verify(fileService).getPresignedUrl(objectName, roomUuid, userUuid);
    }

    @Test
    void getFileUrl_when_missing_userUuid_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        String objectName = "file.txt";

        mockMvc.perform(
                        get("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileService);
    }

    @Test
    void deleteFile_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        String objectName = "file.txt";

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .param("userUuid", userUuid.toString())
                                .param("messageUuid", messageUuid.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(fileService).deleteFile(objectName, roomUuid, userUuid, messageUuid);
    }

    @Test
    void deleteFile_when_missing_messageUuid_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String objectName = "file.txt";

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .param("userUuid", userUuid.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileService);
    }
}
