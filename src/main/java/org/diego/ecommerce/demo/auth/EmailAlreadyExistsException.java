package org.diego.ecommerce.demo.auth;

import org.diego.ecommerce.demo.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "EMAIL_ALREADY_EXISTS";
    }
}
