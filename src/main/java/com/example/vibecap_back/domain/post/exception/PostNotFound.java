package com.example.vibecap_back.domain.post.exception;

public class PostNotFound extends RuntimeException {

    public PostNotFound(String message) {
        super(message);
    }
}
