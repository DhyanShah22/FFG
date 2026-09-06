package com.antarang.cap.domain.entity;

import com.antarang.cap.common.BaseEntity;
import com.antarang.cap.domain.enums.OrgUnitType;
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

import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "org_units",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"})
)
public class OrgUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_org_unit_id")
    private OrgUnit parentOrgUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_unit_type", nullable = false, length = 50)
    private OrgUnitType orgUnitType;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

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

    public OrgUnit getParentOrgUnit() {
        return parentOrgUnit;
    }

    public void setParentOrgUnit(OrgUnit parentOrgUnit) {
        this.parentOrgUnit = parentOrgUnit;
    }

    public OrgUnitType getOrgUnitType() {
        return orgUnitType;
    }

    public void setOrgUnitType(OrgUnitType orgUnitType) {
        this.orgUnitType = orgUnitType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
