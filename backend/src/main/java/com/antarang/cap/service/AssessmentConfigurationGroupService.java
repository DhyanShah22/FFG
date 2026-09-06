package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.Assessment;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroup;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupAssignment;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupItem;
import com.antarang.cap.domain.entity.AssessmentConfigurationGroupOutput;
import com.antarang.cap.domain.entity.AssessmentVersion;
import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.domain.entity.QuestionnaireVersion;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.AssignmentType;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.RoleName;
import com.antarang.cap.dto.request.AddAssessmentConfigurationGroupItemsRequest;
import com.antarang.cap.dto.request.AddAssessmentConfigurationGroupOutputRequest;
import com.antarang.cap.dto.request.CreateAssessmentConfigurationGroupAssignmentRequest;
import com.antarang.cap.dto.request.CreateAssessmentConfigurationGroupRequest;
import com.antarang.cap.dto.request.UpdateAssessmentConfigurationGroupRequest;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupAssignmentResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupDetailResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupItemResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupOutputResponse;
import com.antarang.cap.dto.response.AssessmentConfigurationGroupResponse;
import com.antarang.cap.dto.response.ConfigurationResolutionResponse;
import com.antarang.cap.dto.response.StudentActiveAssessmentConfigurationResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.AssessmentConfigurationGroupAssignmentRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupItemRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupOutputRepository;
import com.antarang.cap.repository.AssessmentConfigurationGroupRepository;
import com.antarang.cap.repository.AssessmentRepository;
import com.antarang.cap.repository.AssessmentVersionRepository;
import com.antarang.cap.repository.IarLogicVersionRepository;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.QuestionnaireVersionRepository;
import com.antarang.cap.repository.ReportTemplateVersionRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.security.SecurityUtils;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AssessmentConfigurationGroupService {

    private final AssessmentConfigurationGroupRepository groupRepository;
    private final AssessmentConfigurationGroupItemRepository itemRepository;
    private final AssessmentConfigurationGroupOutputRepository outputRepository;
    private final AssessmentConfigurationGroupAssignmentRepository assignmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentVersionRepository assessmentVersionRepository;
    private final QuestionnaireVersionRepository questionnaireVersionRepository;
    private final LanguageRepository languageRepository;
    private final IarLogicVersionRepository iarLogicVersionRepository;
    private final ReportTemplateVersionRepository reportTemplateVersionRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    private final ConfigurationResolutionService configurationResolutionService;
    private final NotificationService notificationService;

    public AssessmentConfigurationGroupService(
            AssessmentConfigurationGroupRepository groupRepository,
            AssessmentConfigurationGroupItemRepository itemRepository,
            AssessmentConfigurationGroupOutputRepository outputRepository,
            AssessmentConfigurationGroupAssignmentRepository assignmentRepository,
            AssessmentRepository assessmentRepository,
            AssessmentVersionRepository assessmentVersionRepository,
            QuestionnaireVersionRepository questionnaireVersionRepository,
            LanguageRepository languageRepository,
            IarLogicVersionRepository iarLogicVersionRepository,
            ReportTemplateVersionRepository reportTemplateVersionRepository,
            TenantRepository tenantRepository,
            AuditService auditService,
            ConfigurationResolutionService configurationResolutionService,
            NotificationService notificationService
    ) {
        this.groupRepository = groupRepository;
        this.itemRepository = itemRepository;
        this.outputRepository = outputRepository;
        this.assignmentRepository = assignmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentVersionRepository = assessmentVersionRepository;
        this.questionnaireVersionRepository = questionnaireVersionRepository;
        this.languageRepository = languageRepository;
        this.iarLogicVersionRepository = iarLogicVersionRepository;
        this.reportTemplateVersionRepository = reportTemplateVersionRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
        this.configurationResolutionService = configurationResolutionService;
        this.notificationService = notificationService;
    }

    @Transactional
    public AssessmentConfigurationGroupResponse create(CreateAssessmentConfigurationGroupRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UserPrincipal principal = SecurityUtils.requirePrincipal();

        if (groupRepository.existsByTenantIdAndCodeAndIsDeletedFalse(tenantId, request.code())) {
            throw new BusinessException("Configuration group code already exists", "DUPLICATE_RESOURCE");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        AssessmentConfigurationGroup group = new AssessmentConfigurationGroup();
        group.setTenant(tenant);
        group.setCode(request.code());
        group.setName(request.name());
        group.setDescription(request.description());
        group.setAcademicYear(request.academicYear());
        group.setAssessmentCycle(request.assessmentCycle());
        group.setStatus(LifecycleStatus.DRAFT);
        group.setCreatedBy(principal.getId());
        group.setUpdatedBy(principal.getId());

        AssessmentConfigurationGroup saved = groupRepository.save(group);
        auditService.record(
                "AssessmentConfigurationGroup",
                saved.getId(),
                AuditAction.CREATE,
                null,
                Map.of("code", saved.getCode(), "status", saved.getStatus().name())
        );
        return toGroupResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentConfigurationGroupResponse> list(LifecycleStatus status, String academicYear, int page, int size) {
        UUID tenantId = SecurityUtils.requireTenantId();
        PageRequest pageable = PageRequest.of(page, size);
        Page<AssessmentConfigurationGroup> result;
        if (status != null && academicYear != null && !academicYear.isBlank()) {
            result = groupRepository.findByTenantIdAndStatusAndAcademicYearAndIsDeletedFalse(
                    tenantId, status, academicYear, pageable);
        } else if (status != null) {
            result = groupRepository.findByTenantIdAndStatusAndIsDeletedFalse(tenantId, status, pageable);
        } else if (academicYear != null && !academicYear.isBlank()) {
            result = groupRepository.findByTenantIdAndAcademicYearAndIsDeletedFalse(tenantId, academicYear, pageable);
        } else {
            result = groupRepository.findByTenantIdAndIsDeletedFalse(tenantId, pageable);
        }
        return PageResponse.from(result.map(this::toGroupResponse));
    }

    @Transactional(readOnly = true)
    public AssessmentConfigurationGroupDetailResponse get(UUID groupId) {
        AssessmentConfigurationGroup group = requireGroup(groupId);
        return toDetail(group);
    }

    @Transactional
    public AssessmentConfigurationGroupResponse update(UUID groupId, UpdateAssessmentConfigurationGroupRequest request) {
        AssessmentConfigurationGroup group = requireGroup(groupId);
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (request.name() != null) {
            group.setName(request.name());
        }
        if (request.description() != null) {
            group.setDescription(request.description());
        }
        if (request.academicYear() != null) {
            group.setAcademicYear(request.academicYear());
        }
        if (request.assessmentCycle() != null) {
            group.setAssessmentCycle(request.assessmentCycle());
        }
        group.setUpdatedAt(Instant.now());
        group.setUpdatedBy(principal.getId());
        return toGroupResponse(groupRepository.save(group));
    }

    @Transactional
    public List<AssessmentConfigurationGroupItemResponse> addItems(
            UUID groupId,
            AddAssessmentConfigurationGroupItemsRequest request
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentConfigurationGroup group = requireGroup(groupId);

        for (AddAssessmentConfigurationGroupItemsRequest.Item itemRequest : request.items()) {
            Assessment assessment = assessmentRepository
                    .findByIdAndTenantIdAndIsDeletedFalse(itemRequest.assessmentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

            AssessmentVersion assessmentVersion = assessmentVersionRepository
                    .findByIdAndAssessmentTenantId(itemRequest.assessmentVersionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment version not found"));
            if (!assessmentVersion.getAssessment().getId().equals(assessment.getId())) {
                throw new BusinessException("Assessment version does not belong to assessment", "VALIDATION_ERROR");
            }

            QuestionnaireVersion questionnaireVersion = questionnaireVersionRepository
                    .findByIdAndQuestionnaireTenantId(itemRequest.questionnaireVersionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Questionnaire version not found"));

            Language language = languageRepository.findById(itemRequest.assessmentLanguageId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment language not found"));

            if (itemRequest.iarLogicVersionId() != null) {
                iarLogicVersionRepository.findByIdAndIarLogicConfigTenantId(itemRequest.iarLogicVersionId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("IAR logic version not found"));
            }

            AssessmentConfigurationGroupItem item = itemRepository
                    .findByConfigurationGroupIdAndAssessmentId(group.getId(), assessment.getId())
                    .orElseGet(AssessmentConfigurationGroupItem::new);
            item.setConfigurationGroup(group);
            item.setAssessment(assessment);
            item.setAssessmentVersion(assessmentVersion);
            item.setQuestionnaireVersion(questionnaireVersion);
            item.setIarLogicVersionId(itemRequest.iarLogicVersionId());
            item.setScoringRuleVersionId(itemRequest.scoringRuleVersionId());
            item.setAssessmentLanguage(language);
            item.setRequired(itemRequest.isRequired() == null || itemRequest.isRequired());
            item.setDisplayOrder(itemRequest.displayOrder() == null ? 0 : itemRequest.displayOrder());
            itemRepository.save(item);
        }

        return itemRepository.findByConfigurationGroupIdOrderByDisplayOrderAsc(group.getId()).stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Transactional
    public AssessmentConfigurationGroupOutputResponse addOutput(
            UUID groupId,
            AddAssessmentConfigurationGroupOutputRequest request
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AssessmentConfigurationGroup group = requireGroup(groupId);

        Language language = languageRepository.findById(request.outputLanguageId())
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Output language not found"));

        if (request.reportTemplateVersionId() != null) {
            reportTemplateVersionRepository
                    .findByIdAndReportTemplateTenantId(request.reportTemplateVersionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Report template version not found"));
        }

        AssessmentConfigurationGroupOutput output = new AssessmentConfigurationGroupOutput();
        output.setConfigurationGroup(group);
        output.setReportTemplateVersionId(request.reportTemplateVersionId());
        output.setOutputLanguage(language);
        output.setOutputConfig(request.outputConfig());
        return toOutputResponse(outputRepository.save(output));
    }

    @Transactional
    public AssessmentConfigurationGroupAssignmentResponse assign(
            UUID groupId,
            CreateAssessmentConfigurationGroupAssignmentRequest request
    ) {
        AssessmentConfigurationGroup group = requireGroup(groupId);
        UserPrincipal principal = SecurityUtils.requirePrincipal();

        AssessmentConfigurationGroupAssignment assignment = new AssessmentConfigurationGroupAssignment();
        assignment.setConfigurationGroup(group);
        assignment.setAssignmentType(request.assignmentType());
        assignment.setAssignmentId(request.assignmentId());
        assignment.setEffectiveFrom(request.effectiveFrom());
        assignment.setEffectiveTo(request.effectiveTo());
        assignment.setAssignedBy(principal.getId());
        AssessmentConfigurationGroupAssignment saved = assignmentRepository.save(assignment);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("assignmentType", saved.getAssignmentType().name());
        newValue.put("assignmentId", saved.getAssignmentId().toString());
        auditService.record("AssessmentConfigurationGroup", group.getId(), AuditAction.ASSIGN, null, newValue);

        if (saved.getAssignmentType() == AssignmentType.USER) {
            Map<String, String> variables = new HashMap<>();
            variables.put("groupName", group.getName());
            variables.put("groupCode", group.getCode());
            notificationService.enqueue(saved.getAssignmentId(), "ASSESSMENT_ASSIGNED", variables);
        }

        return toAssignmentResponse(saved);
    }

    @Transactional
    public void deleteAssignment(UUID groupId, UUID assignmentId) {
        requireGroup(groupId);
        AssessmentConfigurationGroupAssignment assignment = assignmentRepository
                .findByIdAndConfigurationGroupId(assignmentId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        assignment.setActive(false);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public AssessmentConfigurationGroupResponse activate(UUID groupId) {
        AssessmentConfigurationGroup group = requireGroup(groupId);
        List<AssessmentConfigurationGroupItem> items =
                itemRepository.findByConfigurationGroupIdOrderByDisplayOrderAsc(group.getId());
        if (items.isEmpty()) {
            throw new BusinessException("Group must have at least one item to activate", "INVALID_STATE");
        }

        LifecycleStatus oldStatus = group.getStatus();
        group.setStatus(LifecycleStatus.ACTIVE);
        group.setUpdatedAt(Instant.now());
        group.setUpdatedBy(SecurityUtils.requirePrincipal().getId());
        AssessmentConfigurationGroup saved = groupRepository.save(group);

        auditService.record(
                "AssessmentConfigurationGroup",
                saved.getId(),
                AuditAction.STATUS_CHANGE,
                Map.of("status", oldStatus.name()),
                Map.of("status", saved.getStatus().name())
        );
        return toGroupResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConfigurationResolutionResponse resolve(UUID studentId, UUID assessmentId) {
        ConfigurationResolutionService.ResolutionResult result =
                configurationResolutionService.resolve(studentId, assessmentId);
        return new ConfigurationResolutionResponse(
                toGroupResponse(result.group()),
                result.matchedItem() != null ? toItemResponse(result.matchedItem()) : null,
                result.assignment().getAssignmentType()
        );
    }

    @Transactional(readOnly = true)
    public StudentActiveAssessmentConfigurationResponse getActiveForStudent(UUID studentId, UUID assessmentId) {
        assertCanAccessStudentConfig(studentId);
        ConfigurationResolutionService.ResolutionResult result =
                configurationResolutionService.resolve(studentId, assessmentId);

        List<AssessmentConfigurationGroupItemResponse> items = itemRepository
                .findByConfigurationGroupIdOrderByDisplayOrderAsc(result.group().getId()).stream()
                .map(this::toItemResponse)
                .toList();
        List<AssessmentConfigurationGroupOutputResponse> outputs = outputRepository
                .findByConfigurationGroupIdAndIsActiveTrue(result.group().getId()).stream()
                .map(this::toOutputResponse)
                .toList();

        return new StudentActiveAssessmentConfigurationResponse(
                toGroupResponse(result.group()),
                items,
                outputs,
                result.assignment().getAssignmentType(),
                result.matchedItem() != null ? toItemResponse(result.matchedItem()) : null
        );
    }

    private void assertCanAccessStudentConfig(UUID studentId) {
        UserPrincipal principal = SecurityUtils.requirePrincipal();
        if (principal.hasRole(RoleName.ADMINISTRATOR)
                || principal.hasRole(RoleName.SUB_ADMIN)
                || principal.hasRole(RoleName.CAREER_COUNSELLOR)
                || principal.hasRole(RoleName.SUPER_ADMIN)) {
            return;
        }
        if (principal.hasRole(RoleName.CAREER_EXPLORER) && principal.getId().equals(studentId)) {
            return;
        }
        throw new BusinessException("Access denied for student configuration", "ACCESS_DENIED");
    }

    private AssessmentConfigurationGroup requireGroup(UUID groupId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return groupRepository.findByIdAndTenantIdAndIsDeletedFalse(groupId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment configuration group not found"));
    }

    private AssessmentConfigurationGroupDetailResponse toDetail(AssessmentConfigurationGroup group) {
        return new AssessmentConfigurationGroupDetailResponse(
                toGroupResponse(group),
                itemRepository.findByConfigurationGroupIdOrderByDisplayOrderAsc(group.getId()).stream()
                        .map(this::toItemResponse).toList(),
                outputRepository.findByConfigurationGroupIdAndIsActiveTrue(group.getId()).stream()
                        .map(this::toOutputResponse).toList(),
                assignmentRepository.findByConfigurationGroupIdAndIsActiveTrue(group.getId()).stream()
                        .map(this::toAssignmentResponse).toList()
        );
    }

    AssessmentConfigurationGroupResponse toGroupResponse(AssessmentConfigurationGroup group) {
        return new AssessmentConfigurationGroupResponse(
                group.getId(),
                group.getTenant().getId(),
                group.getCode(),
                group.getName(),
                group.getDescription(),
                group.getAcademicYear(),
                group.getAssessmentCycle(),
                group.getStatus(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    private AssessmentConfigurationGroupItemResponse toItemResponse(AssessmentConfigurationGroupItem item) {
        return new AssessmentConfigurationGroupItemResponse(
                item.getId(),
                item.getAssessment().getId(),
                item.getAssessmentVersion().getId(),
                item.getQuestionnaireVersion().getId(),
                item.getIarLogicVersionId(),
                item.getScoringRuleVersionId(),
                item.getAssessmentLanguage().getId(),
                item.isRequired(),
                item.getDisplayOrder(),
                item.getCreatedAt()
        );
    }

    private AssessmentConfigurationGroupOutputResponse toOutputResponse(AssessmentConfigurationGroupOutput output) {
        return new AssessmentConfigurationGroupOutputResponse(
                output.getId(),
                output.getReportTemplateVersionId(),
                output.getOutputLanguage().getId(),
                output.getOutputConfig(),
                output.isActive(),
                output.getCreatedAt()
        );
    }

    private AssessmentConfigurationGroupAssignmentResponse toAssignmentResponse(
            AssessmentConfigurationGroupAssignment assignment
    ) {
        return new AssessmentConfigurationGroupAssignmentResponse(
                assignment.getId(),
                assignment.getAssignmentType(),
                assignment.getAssignmentId(),
                assignment.getEffectiveFrom(),
                assignment.getEffectiveTo(),
                assignment.isActive(),
                assignment.getAssignedBy(),
                assignment.getAssignedAt()
        );
    }
}
