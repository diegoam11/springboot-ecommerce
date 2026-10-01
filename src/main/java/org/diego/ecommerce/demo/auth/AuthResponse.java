package org.diego.ecommerce.demo.auth;

import org.diego.ecommerce.demo.user.Role;

public record AuthResponse(
        Long id,
        String email,
        Role role
) {
}
