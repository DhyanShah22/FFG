package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.NotificationTemplate;
import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    @Query("""
            SELECT t FROM NotificationTemplate t
            WHERE t.code = :code AND t.channel = :channel AND t.language.id = :languageId
            AND t.isActive = true
            AND (t.tenant.id = :tenantId OR t.tenant IS NULL)
            ORDER BY CASE WHEN t.tenant.id = :tenantId THEN 0 ELSE 1 END
            """)
    Optional<NotificationTemplate> findBestMatch(
            @Param("tenantId") UUID tenantId,
            @Param("code") String code,
            @Param("channel") NotificationDeliveryChannel channel,
            @Param("languageId") UUID languageId
    );
}
