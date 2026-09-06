package com.antarang.cap.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "assessment_response_options",
        uniqueConstraints = @UniqueConstraint(columnNames = {"response_id", "option_id"})
)
public class AssessmentResponseOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "response_id", nullable = false)
    private AssessmentResponse response;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOption option;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssessmentResponse getResponse() { return response; }
    public void setResponse(AssessmentResponse response) { this.response = response; }
    public QuestionOption getOption() { return option; }
    public void setOption(QuestionOption option) { this.option = option; }
}
