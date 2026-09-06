package com.antarang.cap.domain.entity;

import com.antarang.cap.domain.enums.ClusterMemberType;
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
        name = "organizational_cluster_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cluster_id", "member_type", "member_id"})
)
public class OrganizationalClusterMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id", nullable = false)
    private OrganizationalCluster cluster;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 50)
    private ClusterMemberType memberType;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt = Instant.now();

    @Column(name = "added_by")
    private UUID addedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrganizationalCluster getCluster() {
        return cluster;
    }

    public void setCluster(OrganizationalCluster cluster) {
        this.cluster = cluster;
    }

    public ClusterMemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(ClusterMemberType memberType) {
        this.memberType = memberType;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(UUID addedBy) {
        this.addedBy = addedBy;
    }
}
