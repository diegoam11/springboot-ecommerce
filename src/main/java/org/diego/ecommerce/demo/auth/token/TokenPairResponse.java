package org.diego.ecommerce.demo.auth.token;

public record TokenPairResponse(String accessToken, String refreshToken) {}