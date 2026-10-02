package org.diego.ecommerce.demo.auth.token;

import org.diego.ecommerce.demo.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken create(User user) {
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                user,
                Instant.now().plusMillis(refreshExpiration)
        );
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            // Reuso de un token ya revocado = posible robo. Se cierran TODAS las sesiones del usuario.
            revokeAllForUser(refreshToken.getUser());
            throw new InvalidRefreshTokenException("Refresh token has been revoked. All sessions terminated for security.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(this::revoke);
    }
}