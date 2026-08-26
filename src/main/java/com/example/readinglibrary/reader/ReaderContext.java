package com.example.readinglibrary.reader;

public record ReaderContext(Long readerId) {

    public ReaderContext {
        if (readerId == null || readerId <= 0) {
            throw new IllegalArgumentException(
                    "readerId must be greater than 0"
            );
        }
    }
}