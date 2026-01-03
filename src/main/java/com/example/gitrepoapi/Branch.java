package com.example.gitrepoapi;

import com.fasterxml.jackson.annotation.JsonProperty;

record Branch(@JsonProperty("name") String name,@JsonProperty("commit") Commit commit) {

}
