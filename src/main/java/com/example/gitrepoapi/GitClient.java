package com.example.gitrepoapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GitClient {
    private RestClient restClient;

    @Autowired
    public GitClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Repo> fetchRepositories(String username) {
        return restClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new HttpClientErrorException(response.getStatusCode(), getMessageFromStatusCode(response.getStatusCode().value(),response.getStatusText()));
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<Branch> fetchBranches(String username,String repoName){
        return restClient.get()
                .uri("repos/{username}/{repoName}/branches",username,repoName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new HttpClientErrorException(response.getStatusCode(), getMessageFromStatusCode(response.getStatusCode().value(),response.getStatusText()));
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    private String getMessageFromStatusCode(int statusCode, String statusText){
        String message = switch (statusCode) {
            case 404 -> "User not found";
            case 403 -> "Github API rate limit exceeded";
            default -> "Github client error: " + statusText;
        };
        return message;
    }
}
