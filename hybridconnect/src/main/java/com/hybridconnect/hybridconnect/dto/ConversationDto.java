package com.hybridconnect.hybridconnect.dto;

import java.time.LocalDateTime;

public class ConversationDto {

    private Long otherUserId;
    private String otherUserName;
    private String otherUserPrimaryPhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;

    public ConversationDto() {
    }

    public ConversationDto(Long otherUserId,
            String otherUserName,
            String otherUserPrimaryPhotoUrl,
            String lastMessage,
            LocalDateTime lastMessageAt,
            Long unreadCount) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserPrimaryPhotoUrl = otherUserPrimaryPhotoUrl;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserPrimaryPhotoUrl() {
        return otherUserPrimaryPhotoUrl;
    }

    public void setOtherUserPrimaryPhotoUrl(String otherUserPrimaryPhotoUrl) {
        this.otherUserPrimaryPhotoUrl = otherUserPrimaryPhotoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
