package com.hybridconnect.hybridconnect.dto;

public class LoginResponse {
    public boolean success;
    public String message;
    public Long userId;
    public String role;
    public String token; // ✅ add

    public LoginResponse(boolean success, String message, Long userId, String role, String token) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.role = role;
        this.token = token;
    }
}
