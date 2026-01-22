package com.hybridconnect.hybridconnect.dto;

public class WsTypingRequest {
    public Long toUserId;
    public boolean typing;

    public WsTypingRequest() {
    }

    public WsTypingRequest(Long toUserId, boolean typing) {
        this.toUserId = toUserId;
        this.typing = typing;
    }
}
