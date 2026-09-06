package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.FeedbackVisibility;
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
@Table(name = "counsellor_feedback")
public class CounsellorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counsellor_id", nullable = false)
    private User counsellor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private GeneratedReport report;

    @Column(name = "feedback_text", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "recommendation_notes", columnDefinition = "TEXT")
    private String recommendationNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackVisibility visibility = FeedbackVisibility.INTERNAL;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public User getCounsellor() { return counsellor; }
    public void setCounsellor(User counsellor) { this.counsellor = counsellor; }
    public GeneratedReport getReport() { return report; }
    public void setReport(GeneratedReport report) { this.report = report; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public String getRecommendationNotes() { return recommendationNotes; }
    public void setRecommendationNotes(String recommendationNotes) { this.recommendationNotes = recommendationNotes; }
    public FeedbackVisibility getVisibility() { return visibility; }
    public void setVisibility(FeedbackVisibility visibility) { this.visibility = visibility; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
