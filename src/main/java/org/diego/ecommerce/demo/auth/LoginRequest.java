package org.diego.ecommerce.demo.auth;

public record LoginRequest(
        String email,
        String password
) {
}
