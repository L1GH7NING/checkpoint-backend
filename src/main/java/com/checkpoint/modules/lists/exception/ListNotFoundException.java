package com.checkpoint.modules.lists.exception;

import com.checkpoint.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ListNotFoundException extends AppException {
    public ListNotFoundException(UUID listId) {
        super(HttpStatus.NOT_FOUND, "LIST_NOT_FOUND", "List not found: " + listId);
    }
}