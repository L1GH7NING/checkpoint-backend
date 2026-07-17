package com.checkpoint.modules.lists.exception;

import com.checkpoint.exception.AppException;
import org.springframework.http.HttpStatus;

public class ListAccessDeniedException extends AppException {
    public ListAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "LIST_ACCESS_DENIED", "You do not have access to this list");
    }
}