package com.biblioteca.libraryspring.dto;

public class TokenResponse {

    private final String accessToken;
    private final String tokenType = "bearer";

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
