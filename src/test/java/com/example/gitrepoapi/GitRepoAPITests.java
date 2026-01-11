package com.example.gitrepoapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GitRepoAPITests {
    private WireMockServer wireMockServer;
    private ApiService apiService;
    private GitClient gitClient;
    private StopWatch watch;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.setGlobalFixedDelay(1000);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8089")
                .build();
        gitClient = new GitClient(restClient);
        apiService = new ApiService(gitClient);
        watch = new StopWatch();
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
        watch.reset();
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

        watch.start();
        List<Repo> result = gitClient.fetchRepositories("testuser");
        watch.stop();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cool-project", result.get(0).name());

        assertThat(watch.getTime()).as("Delay veryfication").isBetween(1000L,1500L);
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

        watch.start();
        List<Branch> result = gitClient.fetchBranches("testuser", "cool-project");
        watch.stop();

        assertEquals(1, result.size());
        assertEquals("main", result.get(0).name());
        assertEquals("abc123sha", result.get(0).commit().sha());
        assertThat(watch.getTime()).as("Delay veryfication").isBetween(1000L,1500L);

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        stubFor(get(urlMatching("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        watch.start();
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            gitClient.fetchRepositories("nonexistent");
        });
        watch.stop();

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getMessage().contains("User not found"));
        assertThat(watch.getTime()).as("Delay veryfication").isBetween(1000L,1500L);
    }

    @Test
    void shouldReturnProperMappedObcjetBetween2000And2500ms() {
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
                                },
                                {
                                    "name": "pacman",
                                    "owner": { "login": "testuser" },
                                    "fork": true
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
        watch.start();
        List<Repo> result = apiService.getAllRepos("testuser");
        watch.stop();

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
        verify(3,getRequestedFor(urlMatching(".*")));
        assertThat(watch.getTime()).as("Delay veryfication").isBetween(2000L,2500L);
    }

}


