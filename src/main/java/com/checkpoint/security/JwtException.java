package com.checkpoint.security;

/**
 * Custom exception for JWT validation errors.
 * Provides detailed information about why JWT validation failed.
 */
public class JwtException extends RuntimeException {

    public enum ErrorType {
        TOKEN_MISSING("Token is missing"),
        TOKEN_MALFORMED("Token is malformed"),
        TOKEN_EXPIRED("Token is expired"),
        TOKEN_INVALID_SIGNATURE("Token signature is invalid"),
        TOKEN_INVALID("Token is invalid"),
        TOKEN_UNSUPPORTED("Token format is unsupported");

        private final String message;

        ErrorType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final ErrorType errorType;

    public JwtException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public JwtException(ErrorType errorType, String detail) {
        super(errorType.getMessage() + (detail != null ? " - " + detail : ""));
        this.errorType = errorType;
    }

    public JwtException(ErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}

