package com.antarang.cap.service;

import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.AttemptStatusHistory;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.repository.AttemptStatusHistoryRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AttemptStatusHistoryService {

    private final AttemptStatusHistoryRepository statusHistoryRepository;

    public AttemptStatusHistoryService(AttemptStatusHistoryRepository statusHistoryRepository) {
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public void record(AssessmentAttempt attempt, AttemptStatus oldStatus, AttemptStatus newStatus, String remarks) {
        UUID changedBy = null;
        try {
            changedBy = SecurityUtils.requirePrincipal().getId();
        } catch (RuntimeException ignored) {
            // system-driven transitions may run without a principal
        }
        AttemptStatusHistory history = new AttemptStatusHistory();
        history.setAttempt(attempt);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setRemarks(remarks);
        statusHistoryRepository.save(history);
    }
}
