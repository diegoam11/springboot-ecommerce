package org.diego.ecommerce.demo.auth;

import io.jsonwebtoken.JwtException;
import org.diego.ecommerce.demo.auth.token.RefreshToken;
import org.diego.ecommerce.demo.auth.token.RefreshTokenService;
import org.diego.ecommerce.demo.auth.token.TokenPairResponse;
import org.diego.ecommerce.demo.shared.security.JwtService;
import org.diego.ecommerce.demo.shared.security.TokenBlacklistService;
import org.diego.ecommerce.demo.user.Role;
import org.diego.ecommerce.demo.user.User;
import org.diego.ecommerce.demo.user.UserDetailsImpl;
import org.diego.ecommerce.demo.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService tokenBlacklistService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse register(RegisterRequest request){
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(accessToken, refreshToken.getToken(), user.getId(), user.getEmail(), user.getRole());
    }

    public TokenPairResponse refresh(String oldRefreshToken){
        RefreshToken validToken = refreshTokenService.validate(oldRefreshToken);
        User user = validToken.getUser();

        // Rotacion: el token usado queda inservible, se emite uno nuevo
        refreshTokenService.revoke(validToken);
        RefreshToken newRefreshToken = refreshTokenService.create(user);

        String newAccessToken = jwtService.generateToken(new UserDetailsImpl(user));

        return new TokenPairResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(String accessToken, String refreshToken){
        RefreshToken token = refreshTokenService.validate(refreshToken);
        refreshTokenService.revoke(token);
        blacklistAccessToken(accessToken);
    }

    private void blacklistAccessToken(String accessToken){
        try {
            String jti = jwtService.extractJti(accessToken);
            Duration ttl = jwtService.getRemainingValidity(accessToken);
            tokenBlacklistService.blacklist(jti, ttl);
        } catch (JwtException | IllegalArgumentException ex) {
            // Access token ya invalido/expirado: no hay nada que blacklistear.
        }
    }

    private AuthResponse toResponse(User user){
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole());
    }
}