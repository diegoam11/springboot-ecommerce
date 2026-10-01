package org.diego.ecommerce.demo.auth;

public record RegisterRequest(
        String email,
        String password
) {
}
