package org.diego.ecommerce.demo.auth;

import org.diego.ecommerce.demo.user.Role;

public record LoginResponse(
        String token,
        Long id,
        String email,
        Role role
) {
}
