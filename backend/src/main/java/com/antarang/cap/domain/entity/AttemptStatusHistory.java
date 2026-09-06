package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.AttemptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attempt_status_history")
public class AttemptStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private AttemptStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AttemptStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public void setAttempt(AssessmentAttempt attempt) { this.attempt = attempt; }
    public AttemptStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(AttemptStatus oldStatus) { this.oldStatus = oldStatus; }
    public AttemptStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AttemptStatus newStatus) { this.newStatus = newStatus; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
    public UUID getChangedBy() { return changedBy; }
    public void setChangedBy(UUID changedBy) { this.changedBy = changedBy; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
