package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Career;
import com.antarang.cap.domain.entity.CareerCluster;
import com.antarang.cap.domain.entity.CareerMapping;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.dto.request.CreateCareerClusterRequest;
import com.antarang.cap.dto.request.CreateCareerMappingRequest;
import com.antarang.cap.dto.request.CreateCareerRequest;
import com.antarang.cap.dto.request.UpdateCareerClusterRequest;
import com.antarang.cap.dto.request.UpdateCareerMappingRequest;
import com.antarang.cap.dto.response.CareerClusterResponse;
import com.antarang.cap.dto.response.CareerMappingResponse;
import com.antarang.cap.dto.response.CareerResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.CareerClusterRepository;
import com.antarang.cap.repository.CareerMappingRepository;
import com.antarang.cap.repository.CareerRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CareerService {

    private final CareerClusterRepository careerClusterRepository;
    private final CareerRepository careerRepository;
    private final CareerMappingRepository careerMappingRepository;
    private final TenantRepository tenantRepository;

    public CareerService(
            CareerClusterRepository careerClusterRepository,
            CareerRepository careerRepository,
            CareerMappingRepository careerMappingRepository,
            TenantRepository tenantRepository
    ) {
        this.careerClusterRepository = careerClusterRepository;
        this.careerRepository = careerRepository;
        this.careerMappingRepository = careerMappingRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public CareerClusterResponse createCluster(CreateCareerClusterRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (careerClusterRepository.existsByTenantIdAndCode(tenantId, request.code())) {
            throw new BusinessException("Career cluster code already exists", "DUPLICATE_RESOURCE");
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        CareerCluster cluster = new CareerCluster();
        cluster.setTenant(tenant);
        cluster.setCode(request.code());
        cluster.setName(request.name());
        cluster.setDescription(request.description());
        return toClusterResponse(careerClusterRepository.save(cluster));
    }

    @Transactional(readOnly = true)
    public List<CareerClusterResponse> listClusters() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return careerClusterRepository.findByTenantIdOrderByNameAsc(tenantId).stream()
                .map(this::toClusterResponse)
                .toList();
    }

    @Transactional
    public CareerClusterResponse updateCluster(UUID id, UpdateCareerClusterRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        CareerCluster cluster = careerClusterRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Career cluster not found"));
        if (request.name() != null) {
            cluster.setName(request.name());
        }
        if (request.description() != null) {
            cluster.setDescription(request.description());
        }
        if (request.isActive() != null) {
            cluster.setActive(request.isActive());
        }
        return toClusterResponse(careerClusterRepository.save(cluster));
    }

    @Transactional
    public CareerResponse createCareer(CreateCareerRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        CareerCluster cluster = careerClusterRepository.findByIdAndTenantId(request.clusterId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Career cluster not found"));
        if (careerRepository.existsByCareerClusterIdAndCode(request.clusterId(), request.code())) {
            throw new BusinessException("Career code already exists in cluster", "DUPLICATE_RESOURCE");
        }
        Career career = new Career();
        career.setCareerCluster(cluster);
        career.setCode(request.code());
        career.setName(request.name());
        career.setDescription(request.description());
        career.setEducationPathway(request.educationPathway());
        career.setSkillsRequired(request.skillsRequired());
        return toCareerResponse(careerRepository.save(career));
    }

    @Transactional(readOnly = true)
    public List<CareerResponse> listCareers(UUID clusterId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (clusterId != null) {
            careerClusterRepository.findByIdAndTenantId(clusterId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Career cluster not found"));
            return careerRepository.findByCareerClusterIdOrderByNameAsc(clusterId).stream()
                    .map(this::toCareerResponse)
                    .toList();
        }
        return careerClusterRepository.findByTenantIdOrderByNameAsc(tenantId).stream()
                .flatMap(cluster -> careerRepository.findByCareerClusterIdOrderByNameAsc(cluster.getId()).stream())
                .map(this::toCareerResponse)
                .toList();
    }

    @Transactional
    public CareerMappingResponse createMapping(CreateCareerMappingRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        Career career = careerRepository.findByIdAndCareerClusterTenantId(request.careerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found"));

        CareerMapping mapping = new CareerMapping();
        mapping.setTenant(tenant);
        mapping.setCareer(career);
        mapping.setInterestDomain(request.interestDomain());
        mapping.setAptitudeDomain(request.aptitudeDomain());
        mapping.setRealityFactor(request.realityFactor());
        mapping.setAspirationFactor(request.aspirationFactor());
        mapping.setMinScore(request.minScore());
        mapping.setMaxScore(request.maxScore());
        mapping.setMappingWeight(request.mappingWeight() != null ? request.mappingWeight() : BigDecimal.ONE);
        mapping.setMappingConfig(request.mappingConfig());
        return toMappingResponse(careerMappingRepository.save(mapping));
    }

    @Transactional(readOnly = true)
    public List<CareerMappingResponse> listMappings(UUID careerId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        List<CareerMapping> mappings = careerId != null
                ? careerMappingRepository.findByTenantIdAndCareerId(tenantId, careerId)
                : careerMappingRepository.findByTenantId(tenantId);
        return mappings.stream().map(this::toMappingResponse).toList();
    }

    @Transactional
    public CareerMappingResponse updateMapping(UUID id, UpdateCareerMappingRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        CareerMapping mapping = careerMappingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Career mapping not found"));
        if (request.interestDomain() != null) {
            mapping.setInterestDomain(request.interestDomain());
        }
        if (request.aptitudeDomain() != null) {
            mapping.setAptitudeDomain(request.aptitudeDomain());
        }
        if (request.realityFactor() != null) {
            mapping.setRealityFactor(request.realityFactor());
        }
        if (request.aspirationFactor() != null) {
            mapping.setAspirationFactor(request.aspirationFactor());
        }
        if (request.minScore() != null) {
            mapping.setMinScore(request.minScore());
        }
        if (request.maxScore() != null) {
            mapping.setMaxScore(request.maxScore());
        }
        if (request.mappingWeight() != null) {
            mapping.setMappingWeight(request.mappingWeight());
        }
        if (request.mappingConfig() != null) {
            mapping.setMappingConfig(request.mappingConfig());
        }
        if (request.isActive() != null) {
            mapping.setActive(request.isActive());
        }
        return toMappingResponse(careerMappingRepository.save(mapping));
    }

    private CareerClusterResponse toClusterResponse(CareerCluster cluster) {
        return new CareerClusterResponse(
                cluster.getId(),
                cluster.getTenant().getId(),
                cluster.getCode(),
                cluster.getName(),
                cluster.getDescription(),
                cluster.isActive()
        );
    }

    private CareerResponse toCareerResponse(Career career) {
        return new CareerResponse(
                career.getId(),
                career.getCareerCluster().getId(),
                career.getCode(),
                career.getName(),
                career.getDescription(),
                career.getEducationPathway(),
                career.getSkillsRequired(),
                career.isActive()
        );
    }

    private CareerMappingResponse toMappingResponse(CareerMapping mapping) {
        return new CareerMappingResponse(
                mapping.getId(),
                mapping.getTenant().getId(),
                mapping.getCareer().getId(),
                mapping.getInterestDomain(),
                mapping.getAptitudeDomain(),
                mapping.getRealityFactor(),
                mapping.getAspirationFactor(),
                mapping.getMinScore(),
                mapping.getMaxScore(),
                mapping.getMappingWeight(),
                mapping.getMappingConfig(),
                mapping.isActive()
        );
    }
}
