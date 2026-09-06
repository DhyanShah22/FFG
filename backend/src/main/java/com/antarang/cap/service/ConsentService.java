package com.antarang.cap.service;

import com.antarang.cap.domain.entity.ConsentRecord;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.dto.request.CreateConsentRequest;
import com.antarang.cap.dto.response.ConsentResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.ConsentRecordRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ConsentService {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMINISTRATOR", "SUB_ADMIN");

    private final ConsentRecordRepository consentRecordRepository;
    private final UserRepository userRepository;

    public ConsentService(ConsentRecordRepository consentRecordRepository, UserRepository userRepository) {
        this.consentRecordRepository = consentRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConsentResponse createConsent(CreateConsentRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        UUID targetUserId = request.userId() != null ? request.userId() : principal.getId();

        User targetUser = resolveAccessibleUser(principal, targetUserId);

        ConsentRecord consent = new ConsentRecord();
        consent.setUser(targetUser);
        consent.setConsentType(request.consentType());
        consent.setConsentTextVersion(request.consentTextVersion());
        consent.setGuardianName(request.guardianName());
        consent.setGuardianContact(request.guardianContact());
        consent.setConsentGiven(request.consentGiven());
        consent.setConsentGivenAt(request.consentGiven() ? Instant.now() : null);
        consent.setMetadata(request.metadata());

        return toResponse(consentRecordRepository.save(consent));
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> listByUser(UUID userId) {
        UserPrincipal principal = getCurrentPrincipal();
        resolveAccessibleUser(principal, userId);

        return consentRecordRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsentResponse withdraw(UUID consentId) {
        UserPrincipal principal = getCurrentPrincipal();
        ConsentRecord consent = consentRecordRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent record not found"));

        resolveAccessibleUser(principal, consent.getUser().getId());

        consent.setConsentGiven(false);
        consent.setConsentWithdrawnAt(Instant.now());

        return toResponse(consentRecordRepository.save(consent));
    }

    private User resolveAccessibleUser(UserPrincipal principal, UUID targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isSelf = targetUser.getId().equals(principal.getId());
        boolean isAdmin = hasAnyAdminRole(principal);

        if (!isSelf && !isAdmin) {
            throw new BusinessException("Access denied for this consent record", "ACCESS_DENIED");
        }

        if (!targetUser.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        return targetUser;
    }

    private boolean hasAnyAdminRole(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_ROLES.stream()
                        .anyMatch(role -> authority.getAuthority().equals("ROLE_" + role)));
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private ConsentResponse toResponse(ConsentRecord consent) {
        return new ConsentResponse(
                consent.getId(),
                consent.getUser().getId(),
                consent.getConsentType(),
                consent.getConsentTextVersion(),
                consent.getGuardianName(),
                consent.getGuardianContact(),
                consent.isConsentGiven(),
                consent.getConsentGivenAt(),
                consent.getConsentWithdrawnAt(),
                consent.getMetadata(),
                consent.getCreatedAt()
        );
    }
}
