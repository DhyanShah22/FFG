package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Configuration;
import com.antarang.cap.domain.entity.ConfigurationGroup;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.dto.request.CreateConfigurationGroupRequest;
import com.antarang.cap.dto.request.CreateConfigurationRequest;
import com.antarang.cap.dto.request.UpdateConfigurationRequest;
import com.antarang.cap.dto.response.ConfigurationGroupResponse;
import com.antarang.cap.dto.response.ConfigurationResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.ConfigurationGroupRepository;
import com.antarang.cap.repository.ConfigurationRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ConfigurationService {

    private final ConfigurationGroupRepository configurationGroupRepository;
    private final ConfigurationRepository configurationRepository;
    private final TenantRepository tenantRepository;

    public ConfigurationService(
            ConfigurationGroupRepository configurationGroupRepository,
            ConfigurationRepository configurationRepository,
            TenantRepository tenantRepository
    ) {
        this.configurationGroupRepository = configurationGroupRepository;
        this.configurationRepository = configurationRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public ConfigurationGroupResponse createGroup(CreateConfigurationGroupRequest request) {
        UserPrincipal principal = getCurrentPrincipal();

        ConfigurationGroup group = new ConfigurationGroup();
        if (request.systemDefined()) {
            group.setTenant(null);
        } else {
            UUID tenantId = request.tenantId() != null ? request.tenantId() : principal.getTenantId();
            if (!tenantId.equals(principal.getTenantId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
            Tenant tenant = tenantRepository.findById(tenantId)
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            group.setTenant(tenant);
        }

        group.setCode(request.code());
        group.setName(request.name());
        group.setDescription(request.description());
        group.setSystemDefined(request.systemDefined());

        return toGroupResponse(configurationGroupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<ConfigurationGroupResponse> listGroups() {
        UserPrincipal principal = getCurrentPrincipal();
        List<ConfigurationGroup> systemGroups = configurationGroupRepository.findByIsSystemDefinedTrueAndIsDeletedFalse();
        List<ConfigurationGroup> tenantGroups = configurationGroupRepository.findByTenantIdAndIsDeletedFalse(principal.getTenantId());

        List<ConfigurationGroup> combined = new ArrayList<>(systemGroups);
        for (ConfigurationGroup group : tenantGroups) {
            if (combined.stream().noneMatch(g -> g.getId().equals(group.getId()))) {
                combined.add(group);
            }
        }

        return combined.stream().map(this::toGroupResponse).toList();
    }

    @Transactional
    public ConfigurationResponse createConfiguration(CreateConfigurationRequest request) {
        ConfigurationGroup group = configurationGroupRepository.findById(request.configurationGroupId())
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Configuration group not found"));

        configurationRepository.findByConfigurationGroupIdAndCodeAndIsDeletedFalse(group.getId(), request.code())
                .ifPresent(existing -> {
                    throw new BusinessException("Configuration code already exists in this group", "DUPLICATE_RESOURCE");
                });

        Configuration configuration = new Configuration();
        configuration.setConfigurationGroup(group);
        configuration.setCode(request.code());
        configuration.setValue(request.value());
        configuration.setDisplayOrder(request.displayOrder());
        configuration.setMetadata(request.metadata());

        return toResponse(configurationRepository.save(configuration));
    }

    @Transactional(readOnly = true)
    public List<ConfigurationResponse> listConfigurations(String groupCode, UUID tenantId) {
        List<Configuration> configurations;
        if (groupCode != null && !groupCode.isBlank()) {
            configurations = configurationRepository
                    .findByConfigurationGroupCodeAndIsDeletedFalseOrderByDisplayOrderAsc(groupCode);
        } else {
            configurations = configurationRepository.findAll().stream()
                    .filter(configuration -> !configuration.isDeleted())
                    .sorted(Comparator.comparingInt(Configuration::getDisplayOrder))
                    .toList();
        }

        if (tenantId != null) {
            configurations = configurations.stream()
                    .filter(configuration -> configuration.getConfigurationGroup().getTenant() != null
                            && configuration.getConfigurationGroup().getTenant().getId().equals(tenantId))
                    .toList();
        }

        return configurations.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ConfigurationResponse updateConfiguration(UUID id, UpdateConfigurationRequest request) {
        Configuration configuration = configurationRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found"));

        if (request.value() != null && !request.value().isBlank()) {
            configuration.setValue(request.value());
        }
        if (request.displayOrder() != null) {
            configuration.setDisplayOrder(request.displayOrder());
        }
        if (request.metadata() != null) {
            configuration.setMetadata(request.metadata());
        }
        if (request.isActive() != null) {
            configuration.setActive(request.isActive());
        }

        return toResponse(configurationRepository.save(configuration));
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private ConfigurationGroupResponse toGroupResponse(ConfigurationGroup group) {
        return new ConfigurationGroupResponse(
                group.getId(),
                group.getTenant() != null ? group.getTenant().getId() : null,
                group.getCode(),
                group.getName(),
                group.getDescription(),
                group.isSystemDefined()
        );
    }

    private ConfigurationResponse toResponse(Configuration configuration) {
        return new ConfigurationResponse(
                configuration.getId(),
                configuration.getConfigurationGroup().getId(),
                configuration.getCode(),
                configuration.getValue(),
                configuration.getDisplayOrder(),
                configuration.getMetadata(),
                configuration.isActive()
        );
    }
}
