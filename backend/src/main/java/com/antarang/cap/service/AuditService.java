package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.AuditLog;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.dto.response.AuditLogResponse;
import com.antarang.cap.repository.AuditLogRepository;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SubAdminScopeService subAdminScopeService;

    public AuditService(AuditLogRepository auditLogRepository, SubAdminScopeService subAdminScopeService) {
        this.auditLogRepository = auditLogRepository;
        this.subAdminScopeService = subAdminScopeService;
    }

    @Transactional
    public void record(String entityName, UUID entityId, AuditAction action, Map<String, Object> oldValue, Map<String, Object> newValue) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        AuditLog log = new AuditLog();
        log.setTenantId(principal.getTenantId());
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(principal.getId());
        log.setPerformedAt(Instant.now());
        enrichFromRequest(log);
        auditLogRepository.save(log);
    }

    @Transactional
    public void recordAs(UUID tenantId, UUID performedBy, String entityName, UUID entityId, AuditAction action,
                         Map<String, Object> oldValue, Map<String, Object> newValue) {
        AuditLog log = new AuditLog();
        log.setTenantId(tenantId);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        enrichFromRequest(log);
        auditLogRepository.save(log);
    }

    private void enrichFromRequest(AuditLog log) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(
            String entityName,
            UUID entityId,
            UUID performedBy,
            Instant fromDate,
            Instant toDate,
            int page,
            int size
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Pageable pageable = PageRequest.of(page, size);

        Specification<AuditLog> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
        if (entityName != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityName"), entityName));
        }
        if (entityId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (performedBy != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("performedBy"), performedBy));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("performedAt"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("performedAt"), toDate));
        }

        UserPrincipal principal = SecurityUtils.requirePrincipal();
        Set<UUID> scopedStudentIds = subAdminScopeService.resolveScopedStudentIds(principal);

        Page<AuditLog> logs = auditLogRepository.findAll(spec, pageable);
        if (principal.hasRole(RoleName.SUB_ADMIN) && scopedStudentIds != null) {
            List<AuditLog> filtered = logs.getContent().stream()
                    .filter(log -> subAdminScopeService.isAuditLogInScope(log, scopedStudentIds, principal.getId()))
                    .toList();
            logs = new PageImpl<>(filtered, pageable, filtered.size());
        }
        return PageResponse.from(logs.map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getEntityName(),
                log.getEntityId(),
                log.getAction(),
                log.getOldValue(),
                log.getNewValue(),
                log.getPerformedBy(),
                log.getPerformedAt(),
                log.getIpAddress()
        );
    }
}
