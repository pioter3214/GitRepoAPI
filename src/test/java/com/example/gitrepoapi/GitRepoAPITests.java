package com.example.gitrepoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWireMock(port = 0)
public class GitRepoAPITests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldReturnErrorWith404Code(){
        stubFor(get(urlEqualTo("/users/nieistniejacy-user/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Not Found\"}")));


        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/repos/nieistniejacy-user", ErrorResponse.class);
    }
}
