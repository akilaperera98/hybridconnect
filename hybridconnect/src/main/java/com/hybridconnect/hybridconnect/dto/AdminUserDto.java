package com.hybridconnect.hybridconnect.dto;

public class AdminUserDto {
    public Long userId;
    public String role;
    public String name;
    public String email;
    public String status;

    public AdminUserDto(Long userId, String role, String name, String email, String status) {
        this.userId = userId;
        this.role = role;
        this.name = name;
        this.email = email;
        this.status = status;
    }
}
