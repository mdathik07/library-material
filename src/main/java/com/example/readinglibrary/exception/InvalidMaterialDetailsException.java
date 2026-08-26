package com.example.readinglibrary.exception;

public class InvalidMaterialDetailsException
        extends RuntimeException {

    private final String code;

    public InvalidMaterialDetailsException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}