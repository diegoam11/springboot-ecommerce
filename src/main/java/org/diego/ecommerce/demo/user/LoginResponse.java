package org.diego.ecommerce.demo.user;

public record LoginResponse(
        String token,
        Long id,
        String email,
        Role role
) {
}
