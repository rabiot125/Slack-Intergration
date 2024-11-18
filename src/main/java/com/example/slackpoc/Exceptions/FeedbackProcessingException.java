package com.example.slackpoc.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FeedbackProcessingException extends RuntimeException {
    public FeedbackProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedbackProcessingException(String message) {
        super(message);
    }
}