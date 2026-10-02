package org.diego.ecommerce.demo.auth;

import org.diego.ecommerce.demo.user.Role;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String email,
        Role role
) {
}
