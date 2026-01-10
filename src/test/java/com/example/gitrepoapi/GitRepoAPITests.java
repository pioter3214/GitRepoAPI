package com.example.gitrepoapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GitRepoAPITests {
    private WireMockServer wireMockServer;
    private ApiService apiService;
    private GitClient gitClient;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8089")
                .build();
        gitClient = new GitClient(restClient);
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void shouldFetchRepositoriesSuccessfully() {
        stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "cool-project",
                                    "owner": { "login": "testuser" },
                                    "fork": false
                                }
                            ]
                        """)));

        List<Repo> result = gitClient.fetchRepositories("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cool-project", result.get(0).name());
        verify(getRequestedFor(urlEqualTo("/users/testuser/repos")));
    }

    @Test
    void shouldFetchBranchesSuccessfully() {
        stubFor(get(urlEqualTo("/repos/testuser/cool-project/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "abc123sha" }
                                }
                            ]
                        """)));

        List<Branch> result = gitClient.fetchBranches("testuser", "cool-project");

        assertEquals(1, result.size());
        assertEquals("main", result.get(0).name());
        assertEquals("abc123sha", result.get(0).commit().sha());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        stubFor(get(urlMatching("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            gitClient.fetchRepositories("nonexistent");
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void shouldReturnProperMappedObcjet() {
        apiService = new ApiService(gitClient);
        stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "cool-project",
                                    "owner": { "login": "testuser" },
                                    "fork": false
                                },
                                {
                                    "name": "calc",
                                    "owner": { "login": "testuser" },
                                    "fork": false
                                }
                            ]
                        """)));

        stubFor(get(urlEqualTo("/repos/testuser/cool-project/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "abc123sha" }
                                }
                            ]
                        """)));

        stubFor(get(urlEqualTo("/repos/testuser/calc/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "abc321sha" }
                                },
                                {
                                    "name": "refactor",
                                    "commit": { "sha": "abc222sha" }
                                }
                            ]
                        """)));

        List<Repo> result = apiService.getAllRepos("testuser");

        assertEquals(2, result.size());
        Repo repo1 = result.get(0);
        Repo repo2 = result.get(1);


        assertEquals("cool-project", repo1.name());
        assertEquals("testuser", repo1.owner().login());
        assertFalse(repo1.fork());

        assertEquals(1, repo1.branches().size());
        assertEquals("main", repo1.branches().get(0).name());
        assertEquals("abc123sha", repo1.branches().get(0).commit().sha());

        assertEquals("calc", repo2.name());
        assertEquals("testuser", repo2.owner().login());
        assertFalse(repo2.fork());

        assertEquals(2, repo2.branches().size());
        assertEquals("main", repo2.branches().get(0).name());
        assertEquals("abc321sha", repo2.branches().get(0).commit().sha());

        assertEquals("refactor", repo2.branches().get(1).name());
        assertEquals("abc222sha", repo2.branches().get(1).commit().sha());
    }
}


