package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, UUID> {

    List<AssignmentHistory> findByStudentIdOrderByPerformedAtDesc(UUID studentId);

    List<AssignmentHistory> findByAssignmentIdOrderByPerformedAtDesc(UUID assignmentId);
}
