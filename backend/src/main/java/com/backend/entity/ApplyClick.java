package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ✅ ADD THIS IMPORT
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apply_clicks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ApplyClick extends BaseEntity {

    @JsonIgnore  // ✅ ADD THIS - Prevents infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore  // ✅ ADD THIS - Prevents infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Boolean applied = false;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = clickedAt.plusDays(30);
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}