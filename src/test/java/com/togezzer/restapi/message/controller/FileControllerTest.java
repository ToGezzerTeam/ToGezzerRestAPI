package com.togezzer.restapi.message.controller;

import com.togezzer.restapi.auth.TestAuthTokenFactory;
import com.togezzer.restapi.auth.service.JwtService;
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

    @Autowired
    private JwtService jwtService;

    private String authHeader() {
        return TestAuthTokenFactory.createBearerToken(jwtService);
    }

    @Test
    void uploadFile_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/messages/{roomUuid}/files", roomUuid)
                                .header("Authorization", authHeader())
                                .file(filePart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isNoContent());

        verify(fileService).uploadFile(any(MultipartFile.class), eq(roomUuid));
    }

    @Test
    void getFileUrl_returns200_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        String objectName = "file.txt";

        String expectedUrl = "http://minio/presigned-url";

        org.mockito.Mockito.when(fileService.getPresignedUrl(objectName, roomUuid)).thenReturn(expectedUrl);

        mockMvc.perform(
                        get("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .header("Authorization", authHeader())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("\"http://minio/presigned-url\""));

        verify(fileService).getPresignedUrl(objectName, roomUuid);
    }


    @Test
    void deleteFile_returns204_and_calls_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();
        String objectName = "file.txt";

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .param("messageUuid", messageUuid.toString())
                                .header("Authorization", authHeader())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(fileService).deleteFile(objectName, roomUuid, messageUuid);
    }

    @Test
    void deleteFile_when_missing_messageUuid_returns400_and_does_not_call_service() throws Exception {
        UUID roomUuid = UUID.randomUUID();
        String objectName = "file.txt";

        mockMvc.perform(
                        delete("/api/messages/{roomUuid}/files/{objectName}", roomUuid, objectName)
                                .header("Authorization", authHeader())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileService);
    }
}
