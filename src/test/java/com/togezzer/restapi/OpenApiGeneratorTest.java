package com.togezzer.restapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiGeneratorTest {

    @LocalServerPort
    private int port;

    @Test
    void generateOpenApiSpec() throws Exception {
        String spec = new RestTemplate()
                .getForObject("http://localhost:" + port + "/v3/api-docs", String.class);

        Files.writeString(Path.of("build/openapi.json"), spec);
    }
}
