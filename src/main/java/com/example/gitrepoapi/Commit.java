package com.example.gitrepoapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Commit(@JsonProperty("sha") String sha) {

}
