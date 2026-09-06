package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AuditLog;
import com.antarang.cap.domain.entity.IntegrationEvent;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.ClusterMemberType;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.repository.OrganizationalClusterMemberRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SubAdminScopeService {

    private final UserRepository userRepository;
    private final OrganizationalClusterMemberRepository clusterMemberRepository;

    public SubAdminScopeService(
            UserRepository userRepository,
            OrganizationalClusterMemberRepository clusterMemberRepository
    ) {
        this.userRepository = userRepository;
        this.clusterMemberRepository = clusterMemberRepository;
    }

    /**
     * Returns scoped student/user ids for SUB_ADMIN, or null for tenant-wide access.
     */
    @Transactional(readOnly = true)
    public Set<UUID> resolveScopedStudentIds(UserPrincipal principal) {
        if (!principal.hasRole(RoleName.SUB_ADMIN)) {
            return null;
        }
        return resolveSubAdminStudentIds(principal);
    }

    @Transactional(readOnly = true)
    public Set<UUID> resolveScopedStudentIds() {
        return resolveScopedStudentIds(SecurityUtils.requirePrincipal());
    }

    public boolean isAuditLogInScope(AuditLog log, Set<UUID> scopedStudentIds, UUID principalId) {
        if (scopedStudentIds == null) {
            return true;
        }
        if (principalId != null && principalId.equals(log.getPerformedBy())) {
            return true;
        }
        if ("users".equalsIgnoreCase(log.getEntityName()) && scopedStudentIds.contains(log.getEntityId())) {
            return true;
        }
        UUID studentId = extractStudentIdFromAudit(log);
        if (studentId != null && scopedStudentIds.contains(studentId)) {
            return true;
        }
        if ("AssessmentConfigurationGroup".equals(log.getEntityName()) && log.getNewValue() != null) {
            Object assignmentType = log.getNewValue().get("assignmentType");
            Object assignmentId = log.getNewValue().get("assignmentId");
            if ("USER".equals(String.valueOf(assignmentType)) && assignmentId != null) {
                try {
                    return scopedStudentIds.contains(UUID.fromString(assignmentId.toString()));
                } catch (IllegalArgumentException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isIntegrationEventInScope(IntegrationEvent event, Set<UUID> scopedStudentIds) {
        if (scopedStudentIds == null) {
            return true;
        }
        UUID studentId = extractStudentIdFromEvent(event);
        return studentId != null && scopedStudentIds.contains(studentId);
    }

    private UUID extractStudentIdFromAudit(AuditLog log) {
        UUID fromPayload = readUuidFromMap(log.getNewValue(), "studentId");
        if (fromPayload != null) {
            return fromPayload;
        }
        return readUuidFromMap(log.getOldValue(), "studentId");
    }

    private UUID extractStudentIdFromEvent(IntegrationEvent event) {
        UUID fromResponse = readUuidFromMap(event.getResponsePayload(), "studentId");
        if (fromResponse != null) {
            return fromResponse;
        }
        return readUuidFromMap(event.getRequestPayload(), "studentId");
    }

    private UUID readUuidFromMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        try {
            return UUID.fromString(map.get(key).toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Set<UUID> resolveSubAdminStudentIds(UserPrincipal principal) {
        Set<UUID> studentIds = new HashSet<>();
        ScopeType scopeType = principal.getScopeType();
        UUID scopeId = principal.getScopeId();

        if (scopeType == ScopeType.ORG_UNIT && scopeId != null) {
            userRepository.findByTenantIdAndIsDeletedFalse(principal.getTenantId()).stream()
                    .filter(u -> u.getPrimaryOrgUnit() != null && scopeId.equals(u.getPrimaryOrgUnit().getId()))
                    .map(User::getId)
                    .forEach(studentIds::add);
        } else if (scopeType == ScopeType.CLUSTER && scopeId != null) {
            clusterMemberRepository.findByClusterIdAndIsActiveTrue(scopeId).stream()
                    .filter(m -> m.getMemberType() == ClusterMemberType.USER)
                    .forEach(m -> studentIds.add(m.getMemberId()));
        } else {
            userRepository.findByTenantIdAndIsDeletedFalse(principal.getTenantId()).stream()
                    .map(User::getId)
                    .forEach(studentIds::add);
        }
        return studentIds;
    }
}
