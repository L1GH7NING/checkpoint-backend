package com.checkpoint.modules.library.exception;

public class DuplicateLibraryEntryException extends RuntimeException {
    public DuplicateLibraryEntryException(String message) {
        super(message);
    }
}