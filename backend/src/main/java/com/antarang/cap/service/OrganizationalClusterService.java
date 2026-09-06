package com.antarang.cap.service;

import com.antarang.cap.domain.entity.OrganizationalCluster;
import com.antarang.cap.domain.entity.OrganizationalClusterMember;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.dto.request.AddClusterMemberRequest;
import com.antarang.cap.dto.request.CreateOrganizationalClusterRequest;
import com.antarang.cap.dto.response.ClusterMemberResponse;
import com.antarang.cap.dto.response.OrganizationalClusterResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.OrganizationalClusterMemberRepository;
import com.antarang.cap.repository.OrganizationalClusterRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationalClusterService {

    private final OrganizationalClusterRepository clusterRepository;
    private final OrganizationalClusterMemberRepository clusterMemberRepository;
    private final TenantRepository tenantRepository;

    public OrganizationalClusterService(
            OrganizationalClusterRepository clusterRepository,
            OrganizationalClusterMemberRepository clusterMemberRepository,
            TenantRepository tenantRepository
    ) {
        this.clusterRepository = clusterRepository;
        this.clusterMemberRepository = clusterMemberRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public OrganizationalClusterResponse create(CreateOrganizationalClusterRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        clusterRepository.findByTenantIdAndCodeAndIsDeletedFalse(tenant.getId(), request.code())
                .ifPresent(existing -> {
                    throw new BusinessException("Cluster code already exists", "DUPLICATE_RESOURCE");
                });

        OrganizationalCluster cluster = new OrganizationalCluster();
        cluster.setTenant(tenant);
        cluster.setCode(request.code());
        cluster.setName(request.name());
        cluster.setDescription(request.description());
        cluster.setClusterType(request.clusterType());

        return toResponse(clusterRepository.save(cluster));
    }

    @Transactional
    public ClusterMemberResponse addMember(UUID clusterId, AddClusterMemberRequest request) {
        OrganizationalCluster cluster = getAccessibleCluster(clusterId);

        clusterMemberRepository.findByClusterIdAndMemberTypeAndMemberId(
                        clusterId, request.memberType(), request.memberId())
                .ifPresent(existing -> {
                    throw new BusinessException("Member is already part of this cluster", "DUPLICATE_RESOURCE");
                });

        OrganizationalClusterMember member = new OrganizationalClusterMember();
        member.setCluster(cluster);
        member.setMemberType(request.memberType());
        member.setMemberId(request.memberId());
        member.setAddedBy(getCurrentPrincipal().getId());

        return toMemberResponse(clusterMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<ClusterMemberResponse> listMembers(UUID clusterId) {
        getAccessibleCluster(clusterId);
        return clusterMemberRepository.findByClusterId(clusterId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    private OrganizationalCluster getAccessibleCluster(UUID clusterId) {
        UserPrincipal principal = getCurrentPrincipal();
        OrganizationalCluster cluster = clusterRepository.findById(clusterId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cluster not found"));

        if (!cluster.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }
        return cluster;
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private OrganizationalClusterResponse toResponse(OrganizationalCluster cluster) {
        return new OrganizationalClusterResponse(
                cluster.getId(),
                cluster.getTenant().getId(),
                cluster.getCode(),
                cluster.getName(),
                cluster.getDescription(),
                cluster.getClusterType()
        );
    }

    private ClusterMemberResponse toMemberResponse(OrganizationalClusterMember member) {
        return new ClusterMemberResponse(
                member.getId(),
                member.getCluster().getId(),
                member.getMemberType(),
                member.getMemberId(),
                member.isActive(),
                member.getAddedAt()
        );
    }
}
