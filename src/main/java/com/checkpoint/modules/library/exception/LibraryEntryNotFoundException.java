package com.checkpoint.modules.library.exception;

import java.util.UUID;

public class LibraryEntryNotFoundException extends RuntimeException {
    public LibraryEntryNotFoundException(UUID id) {
        super("Library entry not found: " + id);
    }
}