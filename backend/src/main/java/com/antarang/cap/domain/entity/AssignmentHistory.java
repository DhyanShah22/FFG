package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.AssignmentAction;
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
@Table(name = "assignment_history")
public class AssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private FacilitatorStudentAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "old_facilitator_id")
    private UUID oldFacilitatorId;

    @Column(name = "new_facilitator_id")
    private UUID newFacilitatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentAction action;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FacilitatorStudentAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(FacilitatorStudentAssignment assignment) {
        this.assignment = assignment;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public UUID getOldFacilitatorId() {
        return oldFacilitatorId;
    }

    public void setOldFacilitatorId(UUID oldFacilitatorId) {
        this.oldFacilitatorId = oldFacilitatorId;
    }

    public UUID getNewFacilitatorId() {
        return newFacilitatorId;
    }

    public void setNewFacilitatorId(UUID newFacilitatorId) {
        this.newFacilitatorId = newFacilitatorId;
    }

    public AssignmentAction getAction() {
        return action;
    }

    public void setAction(AssignmentAction action) {
        this.action = action;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(UUID performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
