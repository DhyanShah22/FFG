package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.entity.Benchmark;
import com.antarang.cap.domain.entity.Configuration;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.dto.request.CreateBenchmarkRequest;
import com.antarang.cap.dto.request.UpdateBenchmarkRequest;
import com.antarang.cap.dto.response.BenchmarkResponse;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentRepository;
import com.antarang.cap.repository.BenchmarkRepository;
import com.antarang.cap.repository.ConfigurationRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BenchmarkService {

    private final BenchmarkRepository benchmarkRepository;
    private final AssessmentRepository assessmentRepository;
    private final ConfigurationRepository configurationRepository;
    private final TenantRepository tenantRepository;

    public BenchmarkService(
            BenchmarkRepository benchmarkRepository,
            AssessmentRepository assessmentRepository,
            ConfigurationRepository configurationRepository,
            TenantRepository tenantRepository
    ) {
        this.benchmarkRepository = benchmarkRepository;
        this.assessmentRepository = assessmentRepository;
        this.configurationRepository = configurationRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public List<BenchmarkResponse> list(
            UUID assessmentId,
            String domainCode,
            UUID gradeConfigId,
            UUID ageGroupConfigId
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return benchmarkRepository.findWithFilters(
                tenantId, assessmentId, domainCode, gradeConfigId, ageGroupConfigId, null, true
        ).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BenchmarkResponse create(CreateBenchmarkRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndIsDeletedFalse(request.assessmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        Benchmark benchmark = new Benchmark();
        benchmark.setTenant(tenant);
        benchmark.setAssessment(assessment);
        benchmark.setDomainCode(request.domainCode());
        benchmark.setGradeConfig(resolveConfig(request.gradeConfigId()));
        benchmark.setAgeGroupConfig(resolveConfig(request.ageGroupConfigId()));
        benchmark.setMinScore(request.minScore());
        benchmark.setMaxScore(request.maxScore());
        benchmark.setBenchmarkLabel(request.benchmarkLabel());
        benchmark.setInterpretation(request.interpretation());
        return toResponse(benchmarkRepository.save(benchmark));
    }

    @Transactional
    public BenchmarkResponse update(UUID id, UpdateBenchmarkRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Benchmark benchmark = benchmarkRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Benchmark not found"));

        if (request.domainCode() != null) {
            benchmark.setDomainCode(request.domainCode());
        }
        if (request.gradeConfigId() != null) {
            benchmark.setGradeConfig(resolveConfig(request.gradeConfigId()));
        }
        if (request.ageGroupConfigId() != null) {
            benchmark.setAgeGroupConfig(resolveConfig(request.ageGroupConfigId()));
        }
        if (request.minScore() != null) {
            benchmark.setMinScore(request.minScore());
        }
        if (request.maxScore() != null) {
            benchmark.setMaxScore(request.maxScore());
        }
        if (request.benchmarkLabel() != null) {
            benchmark.setBenchmarkLabel(request.benchmarkLabel());
        }
        if (request.interpretation() != null) {
            benchmark.setInterpretation(request.interpretation());
        }
        if (request.isActive() != null) {
            benchmark.setActive(request.isActive());
        }
        return toResponse(benchmarkRepository.save(benchmark));
    }

    private Configuration resolveConfig(UUID configId) {
        if (configId == null) {
            return null;
        }
        return configurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found"));
    }

    private BenchmarkResponse toResponse(Benchmark benchmark) {
        return new BenchmarkResponse(
                benchmark.getId(),
                benchmark.getTenant().getId(),
                benchmark.getAssessment().getId(),
                benchmark.getDomainCode(),
                benchmark.getGradeConfig() != null ? benchmark.getGradeConfig().getId() : null,
                benchmark.getAgeGroupConfig() != null ? benchmark.getAgeGroupConfig().getId() : null,
                benchmark.getMinScore(),
                benchmark.getMaxScore(),
                benchmark.getBenchmarkLabel(),
                benchmark.getInterpretation(),
                benchmark.isActive(),
                benchmark.getCreatedAt()
        );
    }
}
