package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.OrganizationalClusterMember;
import com.antarang.cap.domain.enums.ClusterMemberType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationalClusterMemberRepository extends JpaRepository<OrganizationalClusterMember, UUID> {

    List<OrganizationalClusterMember> findByClusterId(UUID clusterId);

    List<OrganizationalClusterMember> findByClusterIdAndIsActiveTrue(UUID clusterId);

    Optional<OrganizationalClusterMember> findByClusterIdAndMemberTypeAndMemberId(
            UUID clusterId, ClusterMemberType memberType, UUID memberId);

    List<OrganizationalClusterMember> findByMemberTypeAndMemberIdAndIsActiveTrue(
            ClusterMemberType memberType, UUID memberId);
}
