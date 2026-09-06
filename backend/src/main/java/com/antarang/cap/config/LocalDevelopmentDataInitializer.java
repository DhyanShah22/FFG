package com.antarang.cap.config;

import com.antarang.cap.domain.entity.ConfigurationGroup;
import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.repository.ConfigurationGroupRepository;
import com.antarang.cap.repository.ConfigurationRepository;
import com.antarang.cap.repository.RoleRepository;
import com.antarang.cap.repository.TenantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/**
 * Minimal data required for running the signup flow against the disposable H2
 * database. Production data remains managed exclusively by Flyway migrations.
 */
@Configuration
@Profile("local")
public class LocalDevelopmentDataInitializer {

    @Bean
    CommandLineRunner seedLocalDevelopmentData(
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            ConfigurationGroupRepository configurationGroupRepository,
            ConfigurationRepository configurationRepository
    ) {
        return args -> seed(tenantRepository, roleRepository, configurationGroupRepository, configurationRepository);
    }

    @Transactional
    void seed(
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            ConfigurationGroupRepository configurationGroupRepository,
            ConfigurationRepository configurationRepository
    ) {
        if (tenantRepository.findByCodeAndIsDeletedFalse("demo").isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setCode("demo");
            tenant.setName("Demo Tenant");
            tenant.setDescription("Local development tenant");
            tenantRepository.save(tenant);
        }

        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByCode(roleName).isPresent()) {
                continue;
            }
            Role role = new Role();
            role.setCode(roleName);
            role.setDisplayName(roleName.name().replace('_', ' '));
            role.setDescription("Local development role");
            role.setSystemRole(true);
            roleRepository.save(role);
        }

        ConfigurationGroup genderGroup = configurationGroupRepository.findByCodeAndIsDeletedFalse("GENDER")
                .orElseGet(() -> {
                    ConfigurationGroup group = new ConfigurationGroup();
                    group.setCode("GENDER");
                    group.setName("Gender");
                    group.setDescription("Local development gender values");
                    group.setSystemDefined(true);
                    return configurationGroupRepository.save(group);
                });

        seedConfiguration(configurationRepository, genderGroup, "MALE", "Male", 1);
        seedConfiguration(configurationRepository, genderGroup, "FEMALE", "Female", 2);
        seedConfiguration(configurationRepository, genderGroup, "OTHER", "Other", 3);
    }

    private void seedConfiguration(
            ConfigurationRepository configurationRepository,
            ConfigurationGroup group,
            String code,
            String value,
            int displayOrder
    ) {
        if (configurationRepository.findByConfigurationGroupIdAndCodeAndIsDeletedFalse(group.getId(), code).isPresent()) {
            return;
        }
        com.antarang.cap.domain.entity.Configuration configuration = new com.antarang.cap.domain.entity.Configuration();
        configuration.setConfigurationGroup(group);
        configuration.setCode(code);
        configuration.setValue(value);
        configuration.setDisplayOrder(displayOrder);
        configurationRepository.save(configuration);
    }
}
