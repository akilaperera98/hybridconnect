package com.hybridconnect.hybridconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = { "user1_id", "user2_id" }))
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // always store smaller id in user1, bigger in user2 (avoid duplicates)
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Conversation() {
    }

    public Long getId() {
        return id;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
}
