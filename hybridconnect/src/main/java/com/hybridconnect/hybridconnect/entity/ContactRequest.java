package com.hybridconnect.hybridconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_requests")
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who sent request (female)
    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    // To which profile (male)
    @ManyToOne
    @JoinColumn(name = "to_profile_id", nullable = false)
    private Profile toProfile;

    private String status = "PENDING"; // PENDING / ACCEPTED / REJECTED

    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
    public Long getId() {
        return id;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public Profile getToProfile() {
        return toProfile;
    }

    public void setToProfile(Profile toProfile) {
        this.toProfile = toProfile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
