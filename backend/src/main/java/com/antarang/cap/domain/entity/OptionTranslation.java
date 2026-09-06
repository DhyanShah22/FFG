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
        name = "option_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"option_id", "language_id"})
)
public class OptionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOption option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public QuestionOption getOption() { return option; }
    public void setOption(QuestionOption option) { this.option = option; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
}
