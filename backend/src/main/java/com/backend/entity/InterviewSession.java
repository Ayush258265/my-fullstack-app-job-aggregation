package com.backend.entity;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InterviewSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String topic;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "answered_questions")
    private Integer answeredQuestions = 0;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    public void incrementAnsweredQuestions() {
        this.answeredQuestions = (this.answeredQuestions == null ? 0 : this.answeredQuestions) + 1;
    }
}