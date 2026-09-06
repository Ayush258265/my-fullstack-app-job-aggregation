package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ✅ ADD THIS
import javax.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore  // ✅ ADD THIS
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore  // ✅ ADD THIS
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}