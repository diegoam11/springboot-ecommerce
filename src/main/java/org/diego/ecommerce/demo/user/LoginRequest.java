package org.diego.ecommerce.demo.user;

public record LoginRequest(
        String email,
        String password
) {
}
