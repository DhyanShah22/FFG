package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.ExternalEntityType;
import com.antarang.cap.domain.enums.InternalEntityType;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "external_id_mappings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"external_system", "external_entity_type", "external_entity_id"})
)
public class ExternalIdMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "external_system", nullable = false, length = 100)
    private String externalSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "external_entity_type", nullable = false, length = 100)
    private ExternalEntityType externalEntityType;

    @Column(name = "external_entity_id", nullable = false, length = 200)
    private String externalEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "internal_entity_type", nullable = false, length = 100)
    private InternalEntityType internalEntityType;

    @Column(name = "internal_entity_id", nullable = false)
    private UUID internalEntityId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public String getExternalSystem() { return externalSystem; }
    public void setExternalSystem(String externalSystem) { this.externalSystem = externalSystem; }
    public ExternalEntityType getExternalEntityType() { return externalEntityType; }
    public void setExternalEntityType(ExternalEntityType externalEntityType) { this.externalEntityType = externalEntityType; }
    public String getExternalEntityId() { return externalEntityId; }
    public void setExternalEntityId(String externalEntityId) { this.externalEntityId = externalEntityId; }
    public InternalEntityType getInternalEntityType() { return internalEntityType; }
    public void setInternalEntityType(InternalEntityType internalEntityType) { this.internalEntityType = internalEntityType; }
    public UUID getInternalEntityId() { return internalEntityId; }
    public void setInternalEntityId(UUID internalEntityId) { this.internalEntityId = internalEntityId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
