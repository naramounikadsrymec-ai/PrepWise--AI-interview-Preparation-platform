package com.aiinterview.backend.dto;

public class JwtResponse {

    private String token;
    private String type;
    private Long userId;
    private String name;

    public JwtResponse() {
    }

    public JwtResponse(String type, String token, Long userId, String name) {
        this.type = type;
        this.token = token;
        this.userId = userId;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
