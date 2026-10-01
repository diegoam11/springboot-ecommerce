package org.diego.ecommerce.demo.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();

    public abstract String getErrorCode();
}
