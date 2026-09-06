package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ScoreStatus;
import com.antarang.cap.domain.enums.SubmissionReason;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_attempts")
public class AssessmentAttempt {

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
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_version_id", nullable = false)
    private AssessmentVersion assessmentVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuration_group_id", nullable = false)
    private AssessmentConfigurationGroup configurationGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_version_id", nullable = false)
    private QuestionnaireVersion questionnaireVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iar_logic_version_id")
    private IarLogicVersion iarLogicVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scoring_rule_version_id")
    private ScoringRuleVersion scoringRuleVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_template_version_id")
    private ReportTemplateVersion reportTemplateVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_language_id", nullable = false)
    private Language assessmentLanguage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "output_language_id", nullable = false)
    private Language outputLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "timer_mode", nullable = false, length = 30)
    private TimerMode timerMode = TimerMode.COUNT_UP;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "evaluated_at")
    private Instant evaluatedAt;

    @Column(name = "elapsed_seconds")
    private Integer elapsedSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_reason", length = 50)
    private SubmissionReason submissionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_status", nullable = false, length = 30)
    private ScoreStatus scoreStatus = ScoreStatus.PENDING;

    @Column(name = "is_admin_test_attempt", nullable = false)
    private boolean isAdminTestAttempt = false;

    @Column(name = "exclude_from_analytics", nullable = false)
    private boolean excludeFromAnalytics = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public AssessmentVersion getAssessmentVersion() { return assessmentVersion; }
    public void setAssessmentVersion(AssessmentVersion assessmentVersion) { this.assessmentVersion = assessmentVersion; }
    public AssessmentConfigurationGroup getConfigurationGroup() { return configurationGroup; }
    public void setConfigurationGroup(AssessmentConfigurationGroup configurationGroup) { this.configurationGroup = configurationGroup; }
    public QuestionnaireVersion getQuestionnaireVersion() { return questionnaireVersion; }
    public void setQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) { this.questionnaireVersion = questionnaireVersion; }
    public IarLogicVersion getIarLogicVersion() { return iarLogicVersion; }
    public void setIarLogicVersion(IarLogicVersion iarLogicVersion) { this.iarLogicVersion = iarLogicVersion; }
    public ScoringRuleVersion getScoringRuleVersion() { return scoringRuleVersion; }
    public void setScoringRuleVersion(ScoringRuleVersion scoringRuleVersion) { this.scoringRuleVersion = scoringRuleVersion; }
    public ReportTemplateVersion getReportTemplateVersion() { return reportTemplateVersion; }
    public void setReportTemplateVersion(ReportTemplateVersion reportTemplateVersion) { this.reportTemplateVersion = reportTemplateVersion; }
    public Language getAssessmentLanguage() { return assessmentLanguage; }
    public void setAssessmentLanguage(Language assessmentLanguage) { this.assessmentLanguage = assessmentLanguage; }
    public Language getOutputLanguage() { return outputLanguage; }
    public void setOutputLanguage(Language outputLanguage) { this.outputLanguage = outputLanguage; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public TimerMode getTimerMode() { return timerMode; }
    public void setTimerMode(TimerMode timerMode) { this.timerMode = timerMode; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public Integer getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(Integer elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    public SubmissionReason getSubmissionReason() { return submissionReason; }
    public void setSubmissionReason(SubmissionReason submissionReason) { this.submissionReason = submissionReason; }
    public ScoreStatus getScoreStatus() { return scoreStatus; }
    public void setScoreStatus(ScoreStatus scoreStatus) { this.scoreStatus = scoreStatus; }
    public boolean isAdminTestAttempt() { return isAdminTestAttempt; }
    public void setAdminTestAttempt(boolean adminTestAttempt) { isAdminTestAttempt = adminTestAttempt; }
    public boolean isExcludeFromAnalytics() { return excludeFromAnalytics; }
    public void setExcludeFromAnalytics(boolean excludeFromAnalytics) { this.excludeFromAnalytics = excludeFromAnalytics; }
}
