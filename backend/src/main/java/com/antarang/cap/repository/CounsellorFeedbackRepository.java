package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.CounsellorFeedback;
import com.antarang.cap.domain.enums.FeedbackVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CounsellorFeedbackRepository extends JpaRepository<CounsellorFeedback, UUID> {
    Page<CounsellorFeedback> findByStudentIdAndIsActiveTrueOrderByCreatedAtDesc(UUID studentId, Pageable pageable);

    Page<CounsellorFeedback> findByStudentIdAndVisibilityAndIsActiveTrueOrderByCreatedAtDesc(
            UUID studentId, FeedbackVisibility visibility, Pageable pageable);

    Optional<CounsellorFeedback> findByIdAndTenantId(UUID id, UUID tenantId);
}
