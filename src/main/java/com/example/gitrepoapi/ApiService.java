package com.example.gitrepoapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiService {
    private GitClient gitClient;

    @Autowired
    public ApiService(GitClient gitClient) {
        this.gitClient = gitClient;
    }

    public List<Repo> getAllRepos(String username){
        return gitClient.fetchRepositories(username).stream()
                .filter(repo -> repo.fork() == false)
                .map(repo -> {
                    List<Branch> branches = gitClient.fetchBranches(username,repo.name());
                    return new Repo(repo.name(),repo.owner(), repo.fork(), branches);
                }).toList();
    }
}
