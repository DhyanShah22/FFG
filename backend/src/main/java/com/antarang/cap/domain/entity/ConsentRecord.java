package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.ConsentType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "consent_records")
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 50)
    private ConsentType consentType;

    @Column(name = "consent_text_version", nullable = false, length = 50)
    private String consentTextVersion;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_contact", length = 50)
    private String guardianContact;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven;

    @Column(name = "consent_given_at")
    private Instant consentGivenAt;

    @Column(name = "consent_withdrawn_at")
    private Instant consentWithdrawnAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConsentType getConsentType() {
        return consentType;
    }

    public void setConsentType(ConsentType consentType) {
        this.consentType = consentType;
    }

    public String getConsentTextVersion() {
        return consentTextVersion;
    }

    public void setConsentTextVersion(String consentTextVersion) {
        this.consentTextVersion = consentTextVersion;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianContact() {
        return guardianContact;
    }

    public void setGuardianContact(String guardianContact) {
        this.guardianContact = guardianContact;
    }

    public boolean isConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    public Instant getConsentGivenAt() {
        return consentGivenAt;
    }

    public void setConsentGivenAt(Instant consentGivenAt) {
        this.consentGivenAt = consentGivenAt;
    }

    public Instant getConsentWithdrawnAt() {
        return consentWithdrawnAt;
    }

    public void setConsentWithdrawnAt(Instant consentWithdrawnAt) {
        this.consentWithdrawnAt = consentWithdrawnAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
