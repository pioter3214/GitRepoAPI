package com.example.gitrepoapi;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/github")
public class Controller {
    private ApiService service;

    @Autowired
    public Controller(ApiService service) {
        this.service = service;
    }

    @GetMapping("/{username}/repos")
    public ResponseEntity get(@PathVariable String username){
        return new ResponseEntity(service.getAllRepos(username), HttpStatus.OK);
    }
}
