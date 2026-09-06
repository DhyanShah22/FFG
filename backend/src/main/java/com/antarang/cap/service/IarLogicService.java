package com.antarang.cap.service;

import com.antarang.cap.domain.entity.IarLogicConfig;
import com.antarang.cap.domain.entity.IarLogicVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.VersionStatus;
import com.antarang.cap.dto.request.CreateIarLogicConfigRequest;
import com.antarang.cap.dto.request.CreateIarLogicVersionRequest;
import com.antarang.cap.dto.request.UpdateIarLogicVersionRequest;
import com.antarang.cap.dto.response.IarLogicConfigResponse;
import com.antarang.cap.dto.response.IarLogicVersionResponse;
import com.antarang.cap.dto.response.IarValidationResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.IarLogicConfigRepository;
import com.antarang.cap.repository.IarLogicVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IarLogicService {

    private static final double WEIGHT_TOLERANCE = 0.01;
    private static final String[] WEIGHT_KEYS = {
            "aspirationWeight", "interestWeight", "aptitudeWeight", "realityWeight"
    };

    private final IarLogicConfigRepository configRepository;
    private final IarLogicVersionRepository versionRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public IarLogicService(
            IarLogicConfigRepository configRepository,
            IarLogicVersionRepository versionRepository,
            TenantRepository tenantRepository,
            AuditService auditService
    ) {
        this.configRepository = configRepository;
        this.versionRepository = versionRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional
    public IarLogicConfigResponse createConfig(CreateIarLogicConfigRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (configRepository.existsByTenantIdAndCode(tenantId, request.code())) {
            throw new BusinessException("IAR logic config code already exists", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        IarLogicConfig config = new IarLogicConfig();
        config.setTenant(tenant);
        config.setCode(request.code());
        config.setName(request.name());
        config.setDescription(request.description());
        config.setStatus(LifecycleStatus.DRAFT);
        return toConfigResponse(configRepository.save(config), null);
    }

    @Transactional(readOnly = true)
    public List<IarLogicConfigResponse> listConfigs() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return configRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(config -> {
                    VersionStatus latest = versionRepository
                            .findTopByIarLogicConfigIdOrderByVersionNumberDesc(config.getId())
                            .map(IarLogicVersion::getStatus)
                            .orElse(null);
                    return toConfigResponse(config, latest);
                })
                .toList();
    }

    @Transactional
    public IarLogicVersionResponse createVersion(UUID configId, CreateIarLogicVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        IarLogicConfig config = configRepository.findByIdAndTenantId(configId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("IAR logic config not found"));

        int versionNumber = request.versionNumber() != null
                ? request.versionNumber()
                : versionRepository.findTopByIarLogicConfigIdOrderByVersionNumberDesc(configId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        IarLogicVersion version = new IarLogicVersion();
        version.setIarLogicConfig(config);
        version.setVersionNumber(versionNumber);
        version.setLogicDefinition(request.logicDefinition());
        version.setVersionNotes(request.versionNotes());
        version.setStatus(VersionStatus.DRAFT);

        IarLogicVersion saved = versionRepository.save(version);
        if (config.getCurrentVersionId() == null) {
            config.setCurrentVersionId(saved.getId());
            config.setUpdatedAt(Instant.now());
            configRepository.save(config);
        }
        return toVersionResponse(saved);
    }

    @Transactional
    public IarLogicVersionResponse updateVersion(UUID versionId, UpdateIarLogicVersionRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        IarLogicVersion version = versionRepository.findByIdAndIarLogicConfigTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("IAR logic version not found"));
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT IAR logic versions can be updated", "INVALID_STATE");
        }
        version.setLogicDefinition(request.logicDefinition());
        return toVersionResponse(versionRepository.save(version));
    }

    @Transactional(readOnly = true)
    public IarValidationResponse validate(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        IarLogicVersion version = versionRepository.findByIdAndIarLogicConfigTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("IAR logic version not found"));
        return validateDefinition(version.getLogicDefinition());
    }

    @Transactional
    public IarLogicVersionResponse publish(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UUID userId = SecurityUtils.requirePrincipal().getId();
        IarLogicVersion version = versionRepository.findByIdAndIarLogicConfigTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("IAR logic version not found"));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT IAR logic versions can be published", "INVALID_STATE");
        }

        IarValidationResponse validation = validateDefinition(version.getLogicDefinition());
        if (!validation.valid()) {
            throw new BusinessException("IAR logic validation failed", "VALIDATION_ERROR");
        }

        version.setStatus(VersionStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        version.setPublishedBy(userId);
        IarLogicVersion saved = versionRepository.save(version);

        IarLogicConfig config = saved.getIarLogicConfig();
        config.setCurrentVersionId(saved.getId());
        config.setStatus(LifecycleStatus.ACTIVE);
        config.setUpdatedAt(Instant.now());
        configRepository.save(config);

        auditService.record(
                "IarLogicVersion",
                saved.getId(),
                AuditAction.PUBLISH,
                Map.of("status", VersionStatus.DRAFT.name()),
                Map.of("status", VersionStatus.PUBLISHED.name(), "versionNumber", saved.getVersionNumber())
        );
        return toVersionResponse(saved);
    }

    @Transactional
    public IarLogicVersionResponse retire(UUID versionId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        IarLogicVersion version = versionRepository.findByIdAndIarLogicConfigTenantId(versionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("IAR logic version not found"));

        if (version.getStatus() == VersionStatus.ARCHIVED) {
            throw new BusinessException("IAR logic version is already retired", "INVALID_STATE");
        }

        Map<String, Object> oldValue = Map.of("status", version.getStatus().name());
        version.setStatus(VersionStatus.ARCHIVED);
        IarLogicVersion saved = versionRepository.save(version);

        auditService.record(
                "IarLogicVersion",
                saved.getId(),
                AuditAction.STATUS_CHANGE,
                oldValue,
                Map.of("status", VersionStatus.ARCHIVED.name(), "action", "retire")
        );
        return toVersionResponse(saved);
    }

    IarValidationResponse validateDefinition(Map<String, Object> logicDefinition) {
        List<IarValidationResponse.ValidationError> errors = new ArrayList<>();
        if (logicDefinition == null || logicDefinition.isEmpty()) {
            errors.add(new IarValidationResponse.ValidationError("logicDefinition", "logicDefinition is required"));
            return new IarValidationResponse(false, errors);
        }

        double sum = 0.0;
        for (String key : WEIGHT_KEYS) {
            Object raw = logicDefinition.get(key);
            if (raw == null) {
                errors.add(new IarValidationResponse.ValidationError(key, key + " is required"));
                continue;
            }
            Double value = toDouble(raw);
            if (value == null) {
                errors.add(new IarValidationResponse.ValidationError(key, key + " must be a number"));
                continue;
            }
            if (value < 0) {
                errors.add(new IarValidationResponse.ValidationError(key, key + " must be >= 0"));
            }
            sum += value;
        }

        if (errors.isEmpty() && Math.abs(sum - 1.0) > WEIGHT_TOLERANCE) {
            errors.add(new IarValidationResponse.ValidationError(
                    "weights",
                    "aspiration/interest/aptitude/reality weights must sum to 1.0 (±0.01); current sum=" + sum
            ));
        }

        return new IarValidationResponse(errors.isEmpty(), errors);
    }

    private Double toDouble(Object raw) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(raw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private IarLogicConfigResponse toConfigResponse(IarLogicConfig config, VersionStatus latestVersionStatus) {
        return new IarLogicConfigResponse(
                config.getId(),
                config.getTenant().getId(),
                config.getCode(),
                config.getName(),
                config.getDescription(),
                config.getStatus(),
                config.getCurrentVersionId(),
                latestVersionStatus,
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }

    private IarLogicVersionResponse toVersionResponse(IarLogicVersion version) {
        return new IarLogicVersionResponse(
                version.getId(),
                version.getIarLogicConfig().getId(),
                version.getVersionNumber(),
                version.getLogicDefinition(),
                version.getStatus(),
                version.getPublishedAt(),
                version.getPublishedBy(),
                version.getVersionNotes(),
                version.getCreatedAt()
        );
    }
}
