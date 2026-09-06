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
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "question_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "language_id"})
)
public class QuestionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
}
