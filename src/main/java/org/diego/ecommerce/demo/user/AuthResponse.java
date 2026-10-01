package org.diego.ecommerce.demo.user;

public record AuthResponse(
        Long id,
        String email,
        Role role
) {
}
