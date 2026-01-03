package com.example.gitrepoapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Repo(@JsonProperty("name") String name,
                   @JsonProperty("owner") Owner owner,
                   @JsonProperty(value = "fork",access = JsonProperty.Access.WRITE_ONLY)  boolean fork,
                   List<Branch> branches) {

}
