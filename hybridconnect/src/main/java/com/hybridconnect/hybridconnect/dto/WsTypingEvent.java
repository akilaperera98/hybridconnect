package com.hybridconnect.hybridconnect.dto;

public class WsTypingEvent {
    public Long fromUserId;
    public Long toUserId;
    public boolean typing;

    public WsTypingEvent() {
    }

    public WsTypingEvent(Long fromUserId, Long toUserId, boolean typing) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.typing = typing;
    }
}
