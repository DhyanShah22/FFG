package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {

    Optional<Configuration> findByConfigurationGroupIdAndCodeAndIsDeletedFalse(UUID configurationGroupId, String code);

    List<Configuration> findByConfigurationGroupIdAndIsDeletedFalseOrderByDisplayOrderAsc(UUID configurationGroupId);

    List<Configuration> findByConfigurationGroupCodeAndIsDeletedFalseOrderByDisplayOrderAsc(String groupCode);
}
