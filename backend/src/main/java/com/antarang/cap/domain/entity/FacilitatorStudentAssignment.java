package com.antarang.cap.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "facilitator_student_assignments")
public class FacilitatorStudentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facilitator_id", nullable = false)
    private User facilitator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrgUnit orgUnit;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "unassigned_at")
    private Instant unassignedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public User getFacilitator() {
        return facilitator;
    }

    public void setFacilitator(User facilitator) {
        this.facilitator = facilitator;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public OrgUnit getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getUnassignedAt() {
        return unassignedAt;
    }

    public void setUnassignedAt(Instant unassignedAt) {
        this.unassignedAt = unassignedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
