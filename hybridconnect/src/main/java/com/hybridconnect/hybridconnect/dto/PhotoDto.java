package com.hybridconnect.hybridconnect.dto;

import java.time.LocalDateTime;

public class PhotoDto {
    public Long id;
    public String url;
    public Boolean isPrimary;
    public LocalDateTime createdAt;

    public PhotoDto(Long id, String url, Boolean isPrimary, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
    }
}
