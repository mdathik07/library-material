package com.example.readinglibrary.reader;

import org.springframework.stereotype.Component;

@Component
public class ReaderContextResolver {

    public ReaderContext resolve(Long readerId) {
        return new ReaderContext(readerId);
    }
}