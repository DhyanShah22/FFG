package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.CompletionRule;
import com.antarang.cap.domain.enums.TimerMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "assessment_configurations")
public class AssessmentConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_version_id", nullable = false, unique = true)
    private AssessmentVersion assessmentVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "timer_mode", nullable = false, length = 30)
    private TimerMode timerMode = TimerMode.COUNT_UP;

    @Column(name = "max_duration_minutes")
    private Integer maxDurationMinutes;

    @Column(name = "auto_submit_enabled", nullable = false)
    private boolean autoSubmitEnabled = false;

    @Column(name = "show_timer_to_student", nullable = false)
    private boolean showTimerToStudent = true;

    @Column(name = "allow_resume", nullable = false)
    private boolean allowResume = true;

    @Column(name = "restart_on_interruption", nullable = false)
    private boolean restartOnInterruption = false;

    @Column(name = "allow_reattempt", nullable = false)
    private boolean allowReattempt = false;

    @Column(name = "reattempt_after_days")
    private Integer reattemptAfterDays;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_rule", nullable = false, length = 50)
    private CompletionRule completionRule = CompletionRule.ALL_MANDATORY;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", columnDefinition = "jsonb")
    private Map<String, Object> configJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentVersion getAssessmentVersion() { return assessmentVersion; }
    public void setAssessmentVersion(AssessmentVersion assessmentVersion) { this.assessmentVersion = assessmentVersion; }
    public TimerMode getTimerMode() { return timerMode; }
    public void setTimerMode(TimerMode timerMode) { this.timerMode = timerMode; }
    public Integer getMaxDurationMinutes() { return maxDurationMinutes; }
    public void setMaxDurationMinutes(Integer maxDurationMinutes) { this.maxDurationMinutes = maxDurationMinutes; }
    public boolean isAutoSubmitEnabled() { return autoSubmitEnabled; }
    public void setAutoSubmitEnabled(boolean autoSubmitEnabled) { this.autoSubmitEnabled = autoSubmitEnabled; }
    public boolean isShowTimerToStudent() { return showTimerToStudent; }
    public void setShowTimerToStudent(boolean showTimerToStudent) { this.showTimerToStudent = showTimerToStudent; }
    public boolean isAllowResume() { return allowResume; }
    public void setAllowResume(boolean allowResume) { this.allowResume = allowResume; }
    public boolean isRestartOnInterruption() { return restartOnInterruption; }
    public void setRestartOnInterruption(boolean restartOnInterruption) { this.restartOnInterruption = restartOnInterruption; }
    public boolean isAllowReattempt() { return allowReattempt; }
    public void setAllowReattempt(boolean allowReattempt) { this.allowReattempt = allowReattempt; }
    public Integer getReattemptAfterDays() { return reattemptAfterDays; }
    public void setReattemptAfterDays(Integer reattemptAfterDays) { this.reattemptAfterDays = reattemptAfterDays; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public CompletionRule getCompletionRule() { return completionRule; }
    public void setCompletionRule(CompletionRule completionRule) { this.completionRule = completionRule; }
    public Map<String, Object> getConfigJson() { return configJson; }
    public void setConfigJson(Map<String, Object> configJson) { this.configJson = configJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
