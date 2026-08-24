package com.example.online_exam.exception;

public class RepeatSubmitNotAllowedException extends RuntimeException {
    public RepeatSubmitNotAllowedException(String message) {
        super(message);
    }
}