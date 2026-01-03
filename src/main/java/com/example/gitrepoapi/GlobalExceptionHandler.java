package com.example.gitrepoapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ExceptionMessage> getExceptionMessage(HttpClientErrorException ex){
        ExceptionMessage message = new ExceptionMessage(ex.getStatusCode().value(), ex.getStatusText());
        return new ResponseEntity<>(message,ex.getStatusCode());
    }
}
