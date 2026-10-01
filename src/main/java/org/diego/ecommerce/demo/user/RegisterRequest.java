package org.diego.ecommerce.demo.user;

public record RegisterRequest(
        String email,
        String password
) {
}
