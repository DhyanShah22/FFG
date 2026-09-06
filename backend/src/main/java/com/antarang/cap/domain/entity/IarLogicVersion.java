package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.VersionStatus;
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
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "iar_logic_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"iar_logic_config_id", "version_number"})
)
public class IarLogicVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iar_logic_config_id", nullable = false)
    private IarLogicConfig iarLogicConfig;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "logic_definition", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> logicDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private UUID publishedBy;

    @Column(name = "version_notes", columnDefinition = "TEXT")
    private String versionNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public IarLogicConfig getIarLogicConfig() { return iarLogicConfig; }
    public void setIarLogicConfig(IarLogicConfig iarLogicConfig) { this.iarLogicConfig = iarLogicConfig; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public Map<String, Object> getLogicDefinition() { return logicDefinition; }
    public void setLogicDefinition(Map<String, Object> logicDefinition) { this.logicDefinition = logicDefinition; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public UUID getPublishedBy() { return publishedBy; }
    public void setPublishedBy(UUID publishedBy) { this.publishedBy = publishedBy; }
    public String getVersionNotes() { return versionNotes; }
    public void setVersionNotes(String versionNotes) { this.versionNotes = versionNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
