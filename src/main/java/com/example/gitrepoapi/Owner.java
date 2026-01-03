package com.example.gitrepoapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Owner(@JsonProperty("login") String login) {

}
