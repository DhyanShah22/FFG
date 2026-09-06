package com.antarang.cap.service;

import com.antarang.cap.domain.entity.OrgUnit;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.dto.request.CreateOrgUnitRequest;
import com.antarang.cap.dto.request.UpdateOrgUnitRequest;
import com.antarang.cap.dto.response.OrgUnitResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.OrgUnitRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrgUnitService {

    private final OrgUnitRepository orgUnitRepository;
    private final TenantRepository tenantRepository;

    public OrgUnitService(OrgUnitRepository orgUnitRepository, TenantRepository tenantRepository) {
        this.orgUnitRepository = orgUnitRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public OrgUnitResponse create(CreateOrgUnitRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        OrgUnit parent = null;
        if (request.parentOrgUnitId() != null) {
            parent = orgUnitRepository.findById(request.parentOrgUnitId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent org unit not found"));
            if (!parent.getTenant().getId().equals(tenant.getId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
        }

        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setTenant(tenant);
        orgUnit.setParentOrgUnit(parent);
        orgUnit.setOrgUnitType(request.orgUnitType());
        orgUnit.setCode(request.code());
        orgUnit.setName(request.name());
        orgUnit.setDescription(request.description());
        orgUnit.setAddress(request.address());
        orgUnit.setMetadata(request.metadata());

        return toResponse(orgUnitRepository.save(orgUnit));
    }

    @Transactional
    public OrgUnitResponse update(UUID id, UpdateOrgUnitRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        OrgUnit orgUnit = orgUnitRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));

        if (!orgUnit.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        if (request.orgUnitType() != null) {
            orgUnit.setOrgUnitType(request.orgUnitType());
        }
        if (request.code() != null && !request.code().isBlank()) {
            orgUnit.setCode(request.code());
        }
        if (request.name() != null && !request.name().isBlank()) {
            orgUnit.setName(request.name());
        }
        if (request.description() != null) {
            orgUnit.setDescription(request.description());
        }
        if (request.address() != null) {
            orgUnit.setAddress(request.address());
        }
        if (request.metadata() != null) {
            orgUnit.setMetadata(request.metadata());
        }
        if (request.parentOrgUnitId() != null) {
            if (request.parentOrgUnitId().equals(orgUnit.getId())) {
                throw new BusinessException("Org unit cannot be its own parent", "VALIDATION_ERROR");
            }
            OrgUnit parent = orgUnitRepository.findById(request.parentOrgUnitId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent org unit not found"));
            if (!parent.getTenant().getId().equals(principal.getTenantId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
            orgUnit.setParentOrgUnit(parent);
        }

        return toResponse(orgUnitRepository.save(orgUnit));
    }

    @Transactional(readOnly = true)
    public OrgUnitResponse getById(UUID id) {
        UserPrincipal principal = getCurrentPrincipal();
        OrgUnit orgUnit = orgUnitRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));

        if (!orgUnit.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        return toResponse(orgUnit);
    }

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> list(boolean asTree) {
        UserPrincipal principal = getCurrentPrincipal();
        List<OrgUnit> orgUnits = orgUnitRepository.findByTenantIdAndIsDeletedFalse(principal.getTenantId());

        if (!asTree) {
            return orgUnits.stream().map(this::toResponse).toList();
        }

        return buildTree(orgUnits);
    }

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> tree() {
        return list(true);
    }

    private List<OrgUnitResponse> buildTree(List<OrgUnit> orgUnits) {
        Map<UUID, OrgUnitResponse> byId = new LinkedHashMap<>();
        for (OrgUnit orgUnit : orgUnits) {
            byId.put(orgUnit.getId(), toResponse(orgUnit));
        }

        List<OrgUnitResponse> roots = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnits) {
            OrgUnitResponse node = byId.get(orgUnit.getId());
            UUID parentId = orgUnit.getParentOrgUnit() != null ? orgUnit.getParentOrgUnit().getId() : null;
            if (parentId != null && byId.containsKey(parentId)) {
                byId.get(parentId).children().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private OrgUnitResponse toResponse(OrgUnit orgUnit) {
        UUID parentId = orgUnit.getParentOrgUnit() != null ? orgUnit.getParentOrgUnit().getId() : null;
        return new OrgUnitResponse(
                orgUnit.getId(),
                orgUnit.getTenant().getId(),
                parentId,
                orgUnit.getOrgUnitType(),
                orgUnit.getCode(),
                orgUnit.getName(),
                orgUnit.getDescription(),
                orgUnit.getAddress(),
                orgUnit.getMetadata(),
                new ArrayList<>()
        );
    }
}
