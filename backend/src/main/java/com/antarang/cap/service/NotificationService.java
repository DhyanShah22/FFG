package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.GeneratedReport;
import com.antarang.cap.domain.entity.NotificationLog;
import com.antarang.cap.domain.entity.NotificationTemplate;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
import com.antarang.cap.domain.enums.NotificationLogStatus;
import com.antarang.cap.dto.request.SendNotificationRequest;
import com.antarang.cap.dto.response.NotificationLogResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.NotificationLogRepository;
import com.antarang.cap.repository.NotificationTemplateRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final LanguageRepository languageRepository;

    public NotificationService(
            NotificationTemplateRepository templateRepository,
            NotificationLogRepository logRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            LanguageRepository languageRepository
    ) {
        this.templateRepository = templateRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.languageRepository = languageRepository;
    }

    @Transactional
    public NotificationLogResponse send(SendNotificationRequest request) {
        return toResponse(doSend(
                SecurityUtils.requireTenantId(),
                request.userId(),
                request.templateCode(),
                request.channel(),
                request.variables()
        ));
    }

    @Transactional
    public void enqueue(UUID userId, String templateCode, Map<String, String> variables) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        doSend(user.getTenant().getId(), userId, templateCode, NotificationDeliveryChannel.EMAIL, variables);
    }

    private NotificationLog doSend(
            UUID tenantId,
            UUID userId,
            String templateCode,
            NotificationDeliveryChannel channel,
            Map<String, String> variables
    ) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        Map<String, String> resolvedVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        resolvedVariables.putIfAbsent("firstName", user.getFirstName() != null ? user.getFirstName() : "User");

        UUID resolvedLanguageId = user.getPreferredPlatformLanguageId();
        if (resolvedLanguageId == null) {
            resolvedLanguageId = languageRepository.findByIsDefaultTrueAndIsDeletedFalse()
                    .map(com.antarang.cap.domain.entity.Language::getId)
                    .orElseThrow(() -> new BusinessException("No default language configured", "INVALID_STATE"));
        }
        final UUID languageId = resolvedLanguageId;

        NotificationTemplate template = templateRepository.findBestMatch(tenantId, templateCode, channel, languageId)
                .orElseGet(() -> createFallbackTemplate(tenantId, templateCode, channel, languageId));

        String recipient = channel == NotificationDeliveryChannel.EMAIL
                ? user.getEmail()
                : user.getMobileNumber();
        if (recipient == null || recipient.isBlank()) {
            throw new BusinessException("User has no recipient for channel " + channel, "INVALID_STATE");
        }

        String renderedBody = renderTemplate(template.getBodyTemplate(), resolvedVariables);
        String renderedSubject = template.getSubjectTemplate() != null
                ? renderTemplate(template.getSubjectTemplate(), resolvedVariables)
                : templateCode;

        NotificationLog log = new NotificationLog();
        log.setTenant(user.getTenant());
        log.setUser(user);
        log.setTemplate(template);
        log.setChannel(channel);
        log.setRecipient(recipient);
        log.setStatus(NotificationLogStatus.SENT);
        log.setSentAt(Instant.now());
        Map<String, Object> providerResponse = new HashMap<>();
        providerResponse.put("simulated", true);
        providerResponse.put("subject", renderedSubject);
        providerResponse.put("bodyPreview", renderedBody.length() > 200 ? renderedBody.substring(0, 200) + "..." : renderedBody);
        log.setProviderResponse(providerResponse);

        return logRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationLogResponse> listLogs(
            UUID userId,
            String templateCode,
            NotificationLogStatus status,
            int page,
            int size
    ) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Pageable pageable = PageRequest.of(page, size);

        Specification<NotificationLog> spec = (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (templateCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("template").get("code"), templateCode));
        }

        Page<NotificationLog> logs = logRepository.findAll(spec, pageable);
        return PageResponse.from(logs.map(this::toResponse));
    }

    private NotificationTemplate createFallbackTemplate(
            UUID tenantId,
            String templateCode,
            NotificationDeliveryChannel channel,
            UUID languageId
    ) {
        com.antarang.cap.domain.entity.Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));

        NotificationTemplate template = new NotificationTemplate();
        template.setTenant(tenantRepository.findById(tenantId).orElse(null));
        template.setCode(templateCode);
        template.setChannel(channel);
        template.setSubjectTemplate("Notification: {{firstName}}");
        template.setBodyTemplate("Hello {{firstName}}, this is a notification for " + templateCode + ". {{reportUrl}}");
        template.setLanguage(language);
        return templateRepository.save(template);
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        String result = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return result;
    }

    private NotificationLogResponse toResponse(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId(),
                log.getUser().getId(),
                log.getTemplate().getCode(),
                log.getChannel(),
                log.getRecipient(),
                log.getStatus(),
                log.getSentAt(),
                log.getCreatedAt()
        );
    }
}
