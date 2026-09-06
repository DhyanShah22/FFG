package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.QuestionCategory;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AssessmentType;
import com.antarang.cap.dto.request.CreateQuestionCategoryRequest;
import com.antarang.cap.dto.request.UpdateQuestionCategoryRequest;
import com.antarang.cap.dto.response.QuestionCategoryResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.QuestionCategoryRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class QuestionCategoryService {

    private final QuestionCategoryRepository questionCategoryRepository;
    private final TenantRepository tenantRepository;

    public QuestionCategoryService(
            QuestionCategoryRepository questionCategoryRepository,
            TenantRepository tenantRepository
    ) {
        this.questionCategoryRepository = questionCategoryRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionCategoryResponse> list(AssessmentType assessmentType, int page, int size) {
        UUID tenantId = SecurityUtils.requireTenantId();
        PageRequest pageable = PageRequest.of(page, size);
        Page<QuestionCategory> result = assessmentType != null
                ? questionCategoryRepository.findByTenantIdAndAssessmentType(tenantId, assessmentType, pageable)
                : questionCategoryRepository.findByTenantId(tenantId, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    @Transactional
    public QuestionCategoryResponse create(CreateQuestionCategoryRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = loadTenant(tenantId);

        if (questionCategoryRepository.existsByTenantIdAndAssessmentTypeAndCode(
                tenantId, request.assessmentType(), request.code())) {
            throw new BusinessException("Question category code already exists for this assessment type", "DUPLICATE_RESOURCE");
        }

        QuestionCategory category = new QuestionCategory();
        category.setTenant(tenant);
        category.setAssessmentType(request.assessmentType());
        category.setCode(request.code().trim());
        category.setName(request.name().trim());
        category.setDomainCode(request.domainCode().trim());
        category.setDescription(request.description());

        return toResponse(questionCategoryRepository.save(category));
    }

    @Transactional
    public QuestionCategoryResponse update(UUID categoryId, UpdateQuestionCategoryRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        QuestionCategory category = questionCategoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Question category not found"));

        if (request.name() != null && !request.name().isBlank()) {
            category.setName(request.name().trim());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.domainCode() != null && !request.domainCode().isBlank()) {
            category.setDomainCode(request.domainCode().trim());
        }
        if (request.isActive() != null) {
            category.setActive(request.isActive());
        }

        return toResponse(questionCategoryRepository.save(category));
    }

    private Tenant loadTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    }

    private QuestionCategoryResponse toResponse(QuestionCategory category) {
        return new QuestionCategoryResponse(
                category.getId(),
                category.getTenant().getId(),
                category.getAssessmentType(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getDomainCode(),
                category.isActive()
        );
    }
}
