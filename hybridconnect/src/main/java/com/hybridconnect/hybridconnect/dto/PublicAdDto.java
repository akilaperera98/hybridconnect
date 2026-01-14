package com.hybridconnect.hybridconnect.dto;

import java.time.LocalDateTime;

public class PublicAdDto {
    public Long adId;
    public Long userId;
    public String name;
    public String adType;
    public String title;
    public String description;
    public String location;
    public String priceRange;
    public Boolean featured;
    public LocalDateTime createdAt;

    public PublicAdDto(Long adId, Long userId, String name, String adType, String title,
            String description, String location, String priceRange,
            Boolean featured, LocalDateTime createdAt) {
        this.adId = adId;
        this.userId = userId;
        this.name = name;
        this.adType = adType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.priceRange = priceRange;
        this.featured = featured;
        this.createdAt = createdAt;
    }
}
