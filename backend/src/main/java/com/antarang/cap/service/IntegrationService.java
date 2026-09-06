package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.AssessmentAttempt;
import com.antarang.cap.domain.entity.DomainScore;
import com.antarang.cap.domain.entity.ExternalIdMapping;
import com.antarang.cap.domain.entity.GeneratedReport;
import com.antarang.cap.domain.entity.IntegrationClient;
import com.antarang.cap.domain.entity.IntegrationEvent;
import com.antarang.cap.domain.entity.OrgUnit;
import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.enums.AttemptStatus;
import com.antarang.cap.domain.enums.ExternalEntityType;
import com.antarang.cap.domain.enums.IntegrationEventDirection;
import com.antarang.cap.domain.enums.IntegrationEventStatus;
import com.antarang.cap.domain.enums.IntegrationEventType;
import com.antarang.cap.domain.enums.InternalEntityType;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.domain.enums.UserStatus;
import com.antarang.cap.dto.request.CreateIntegrationClientRequest;
import com.antarang.cap.dto.request.IntegrationCreateStudentRequest;
import com.antarang.cap.dto.response.CreateIntegrationClientResponse;
import com.antarang.cap.dto.response.IntegrationEventResponse;
import com.antarang.cap.dto.response.IntegrationLatestReportResponse;
import com.antarang.cap.dto.response.IntegrationStudentResultsResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentAttemptRepository;
import com.antarang.cap.repository.DomainScoreRepository;
import com.antarang.cap.repository.ExternalIdMappingRepository;
import com.antarang.cap.repository.GeneratedReportRepository;
import com.antarang.cap.repository.IntegrationClientRepository;
import com.antarang.cap.repository.IntegrationEventRepository;
import com.antarang.cap.repository.OrgUnitRepository;
import com.antarang.cap.repository.RoleRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.IntegrationClientPrincipal;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class IntegrationService {

    private final IntegrationClientRepository integrationClientRepository;
    private final ExternalIdMappingRepository externalIdMappingRepository;
    private final IntegrationEventRepository integrationEventRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AssessmentAttemptRepository attemptRepository;
    private final DomainScoreRepository domainScoreRepository;
    private final GeneratedReportRepository generatedReportRepository;
    private final NotificationService notificationService;
    private final SubAdminScopeService subAdminScopeService;

    public IntegrationService(
            IntegrationClientRepository integrationClientRepository,
            ExternalIdMappingRepository externalIdMappingRepository,
            IntegrationEventRepository integrationEventRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            OrgUnitRepository orgUnitRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AssessmentAttemptRepository attemptRepository,
            DomainScoreRepository domainScoreRepository,
            GeneratedReportRepository generatedReportRepository,
            NotificationService notificationService,
            SubAdminScopeService subAdminScopeService
    ) {
        this.integrationClientRepository = integrationClientRepository;
        this.externalIdMappingRepository = externalIdMappingRepository;
        this.integrationEventRepository = integrationEventRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.attemptRepository = attemptRepository;
        this.domainScoreRepository = domainScoreRepository;
        this.generatedReportRepository = generatedReportRepository;
        this.notificationService = notificationService;
        this.subAdminScopeService = subAdminScopeService;
    }

    @Transactional
    public CreateIntegrationClientResponse createClient(CreateIntegrationClientRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        if (integrationClientRepository.existsByTenantIdAndClientCode(tenantId, request.clientCode())) {
            throw new BusinessException("Integration client code already exists", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        String apiKey = generateApiKey();
        IntegrationClient client = new IntegrationClient();
        client.setTenant(tenant);
        client.setClientCode(request.clientCode());
        client.setClientName(request.clientName());
        client.setClientType(request.clientType());
        client.setApiKeyHash(passwordEncoder.encode(apiKey));
        client.setAllowedScopes(request.allowedScopes());
        IntegrationClient saved = integrationClientRepository.save(client);

        return new CreateIntegrationClientResponse(saved.getId(), saved.getClientCode(), apiKey);
    }

    @Transactional
    public Map<String, Object> createStudent(IntegrationCreateStudentRequest request) {
        IntegrationClientPrincipal client = requireIntegrationClient();
        IntegrationClient integrationClient = integrationClientRepository.findById(client.getClientId())
                .orElseThrow(() -> new BusinessException("Integration client not found", "INTEGRATION_UNAUTHORIZED"));

        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("externalSystem", request.externalSystem());
        requestPayload.put("externalStudentId", request.externalStudentId());

        try {
            ExternalIdMapping existing = externalIdMappingRepository
                    .findByExternalSystemAndExternalEntityTypeAndExternalEntityIdAndTenantId(
                            request.externalSystem(),
                            ExternalEntityType.STUDENT,
                            request.externalStudentId(),
                            client.getTenantId()
                    ).orElse(null);

            User student;
            if (existing != null) {
                student = userRepository.findById(existing.getInternalEntityId())
                        .orElseThrow(() -> new ResourceNotFoundException("Mapped student not found"));
                updateStudentProfile(student, request);
            } else {
                student = createStudentUser(client.getTenantId(), request);
                ExternalIdMapping mapping = new ExternalIdMapping();
                mapping.setTenant(integrationClient.getTenant());
                mapping.setExternalSystem(request.externalSystem());
                mapping.setExternalEntityType(ExternalEntityType.STUDENT);
                mapping.setExternalEntityId(request.externalStudentId());
                mapping.setInternalEntityType(InternalEntityType.USER);
                mapping.setInternalEntityId(student.getId());
                externalIdMappingRepository.save(mapping);
            }

            Map<String, String> variables = new HashMap<>();
            variables.put("firstName", student.getFirstName() != null ? student.getFirstName() : "Student");
            notificationService.enqueue(student.getId(), "ACCOUNT_CREATED", variables);

            Map<String, Object> response = Map.of(
                    "studentId", student.getId(),
                    "externalStudentId", request.externalStudentId(),
                    "email", student.getEmail()
            );
            logEvent(integrationClient, IntegrationEventType.STUDENT_CREATE, requestPayload, response, IntegrationEventStatus.SUCCESS, null);
            return response;
        } catch (Exception ex) {
            logEvent(integrationClient, IntegrationEventType.STUDENT_CREATE, requestPayload, null,
                    IntegrationEventStatus.FAILED, ex.getMessage());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public IntegrationStudentResultsResponse getStudentResults(String externalStudentId) {
        IntegrationClientPrincipal client = requireIntegrationClient();
        UUID studentId = resolveStudentId(client.getTenantId(), externalStudentId);

        AssessmentAttempt attempt = attemptRepository
                .findTopByStudentIdAndStatusOrderBySubmittedAtDesc(studentId, AttemptStatus.SUBMITTED)
                .orElse(null);

        List<Map<String, Object>> scores = List.of();
        UUID attemptId = null;
        String attemptStatus = "NONE";
        if (attempt != null) {
            attemptId = attempt.getId();
            attemptStatus = attempt.getStatus().name();
            scores = domainScoreRepository.findByAttemptId(attempt.getId()).stream()
                    .map(this::toScoreMap)
                    .toList();
        }

        IntegrationClient integrationClient = integrationClientRepository.findById(client.getClientId()).orElseThrow();
        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("studentId", studentId);
        responsePayload.put("attemptId", attemptId);
        logEvent(integrationClient, IntegrationEventType.RESULT_FETCH,
                Map.of("externalStudentId", externalStudentId), responsePayload,
                IntegrationEventStatus.SUCCESS, null);

        return new IntegrationStudentResultsResponse(studentId, externalStudentId, attemptStatus, attemptId, scores);
    }

    @Transactional(readOnly = true)
    public IntegrationLatestReportResponse getLatestReport(String externalStudentId) {
        IntegrationClientPrincipal client = requireIntegrationClient();
        UUID studentId = resolveStudentId(client.getTenantId(), externalStudentId);

        GeneratedReport report = generatedReportRepository.findTopByStudentIdOrderByGeneratedAtDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No report found for student"));

        IntegrationClient integrationClient = integrationClientRepository.findById(client.getClientId()).orElseThrow();
        logEvent(integrationClient, IntegrationEventType.REPORT_FETCH,
                Map.of("externalStudentId", externalStudentId),
                Map.of("reportId", report.getId(), "studentId", studentId), IntegrationEventStatus.SUCCESS, null);

        return new IntegrationLatestReportResponse(
                report.getId(),
                studentId,
                report.getFileName(),
                "/api/v1/reports/" + report.getId() + "/download",
                report.getGeneratedAt()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<IntegrationEventResponse> listEvents(
            UUID integrationClientId,
            IntegrationEventType eventType,
            IntegrationEventStatus status,
            Instant fromDate,
            Instant toDate,
            int page,
            int size
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Pageable pageable = PageRequest.of(page, size);

        Specification<IntegrationEvent> spec = (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
        if (integrationClientId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("integrationClient").get("id"), integrationClientId));
        }
        if (eventType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), eventType));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
        }

        Page<IntegrationEvent> events = integrationEventRepository.findAll(spec, pageable);

        UserPrincipal principal = SecurityUtils.requirePrincipal();
        Set<UUID> scopedStudentIds = subAdminScopeService.resolveScopedStudentIds(principal);
        if (scopedStudentIds != null) {
            List<IntegrationEvent> filtered = events.getContent().stream()
                    .filter(event -> subAdminScopeService.isIntegrationEventInScope(event, scopedStudentIds))
                    .toList();
            events = new PageImpl<>(filtered, pageable, filtered.size());
        }

        return PageResponse.from(events.map(this::toEventResponse));
    }

    public IntegrationClientPrincipal authenticateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        for (IntegrationClient client : integrationClientRepository.findByIsActiveTrueAndApiKeyHashIsNotNull()) {
            if (client.getApiKeyHash() != null && passwordEncoder.matches(apiKey, client.getApiKeyHash())) {
                return new IntegrationClientPrincipal(client.getId(), client.getTenant().getId(), client.getClientCode());
            }
        }
        return null;
    }

    private User createStudentUser(UUID tenantId, IntegrationCreateStudentRequest request) {
        if (request.email() != null && userRepository.existsByTenantIdAndEmailAndIsDeletedFalse(tenantId, request.email())) {
            throw new BusinessException("Email already registered", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.email() != null ? request.email() : request.externalStudentId() + "@integration.local");
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMobileNumber(request.mobileNumber());
        user.setProfileType(ProfileType.CAREER_EXPLORER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        if (request.primaryOrgUnitId() != null) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.primaryOrgUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));
            user.setPrimaryOrgUnit(orgUnit);
        }

        User saved = userRepository.save(user);
        assignExplorerRole(saved);
        return saved;
    }

    private void updateStudentProfile(User student, IntegrationCreateStudentRequest request) {
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        if (request.email() != null) {
            student.setEmail(request.email());
        }
        if (request.mobileNumber() != null) {
            student.setMobileNumber(request.mobileNumber());
        }
        userRepository.save(student);
    }

    private void assignExplorerRole(User user) {
        Role role = roleRepository.findByCode(RoleName.CAREER_EXPLORER)
                .orElseThrow(() -> new ResourceNotFoundException("Career explorer role not found"));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setScopeType(ScopeType.TENANT);
        userRole.setActive(true);
        user.getUserRoles().add(userRole);
        userRepository.save(user);
    }

    private UUID resolveStudentId(UUID tenantId, String externalStudentId) {
        return externalIdMappingRepository.findAll().stream()
                .filter(m -> m.getTenant().getId().equals(tenantId))
                .filter(m -> m.getExternalEntityType() == ExternalEntityType.STUDENT)
                .filter(m -> m.getExternalEntityId().equals(externalStudentId))
                .findFirst()
                .map(ExternalIdMapping::getInternalEntityId)
                .orElseThrow(() -> new ResourceNotFoundException("External student mapping not found"));
    }

    private IntegrationClientPrincipal requireIntegrationClient() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof IntegrationClientPrincipal principal) {
            return principal;
        }
        throw new BusinessException("Integration API key required", "INTEGRATION_UNAUTHORIZED");
    }

    private void logEvent(
            IntegrationClient client,
            IntegrationEventType eventType,
            Map<String, Object> requestPayload,
            Map<String, Object> responsePayload,
            IntegrationEventStatus status,
            String errorMessage
    ) {
        IntegrationEvent event = new IntegrationEvent();
        event.setTenant(client.getTenant());
        event.setIntegrationClient(client);
        event.setEventType(eventType);
        event.setDirection(IntegrationEventDirection.INBOUND);
        event.setRequestPayload(requestPayload);
        event.setResponsePayload(responsePayload);
        event.setStatus(status);
        event.setErrorMessage(errorMessage);
        integrationEventRepository.save(event);
    }

    private Map<String, Object> toScoreMap(DomainScore score) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("domainCode", score.getDomainCode());
        map.put("rawScore", score.getRawScore());
        map.put("normalizedScore", score.getNormalizedScore());
        if (score.getBenchmark() != null) {
            map.put("benchmarkLabel", score.getBenchmark().getBenchmarkLabel().name());
        }
        return map;
    }

    private IntegrationEventResponse toEventResponse(IntegrationEvent event) {
        return new IntegrationEventResponse(
                event.getId(),
                event.getIntegrationClient().getId(),
                event.getEventType(),
                event.getDirection(),
                event.getStatus(),
                event.getErrorMessage(),
                event.getCreatedAt()
        );
    }

    private String generateApiKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "cap_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
