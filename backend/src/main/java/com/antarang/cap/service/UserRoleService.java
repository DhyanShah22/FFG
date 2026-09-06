package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.dto.request.AssignRolesRequest;
import com.antarang.cap.dto.response.UserResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.RoleRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.repository.UserRoleRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditService auditService;

    public UserRoleService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse assignRoles(UUID userId, AssignRolesRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        for (AssignRolesRequest.RoleAssignment assignment : request.roles()) {
            Role role = roleRepository.findById(assignment.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + assignment.roleId()));

            ScopeType scopeType = assignment.scopeType() != null ? assignment.scopeType() : ScopeType.GLOBAL;
            UUID scopeId = assignment.scopeId();

            boolean alreadyAssigned = user.getUserRoles().stream()
                    .anyMatch(ur -> !ur.isDeleted()
                            && ur.getRole().getId().equals(role.getId())
                            && ur.getScopeType() == scopeType
                            && Objects.equals(ur.getScopeId(), scopeId));

            if (alreadyAssigned) {
                continue;
            }

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setScopeType(scopeType);
            userRole.setScopeId(scopeId);
            user.getUserRoles().add(userRole);
        }

        User saved = userRepository.save(user);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("userId", userId.toString());
        newValue.put("rolesAssigned", request.roles().size());
        auditService.record("user_roles", userId, AuditAction.ASSIGN, null, newValue);

        return toResponse(userRepository.findByIdWithRoles(saved.getId()).orElse(saved));
    }

    @Transactional
    public UserResponse removeRole(UUID userId, UUID roleId) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        List<UserRole> matches = user.getUserRoles().stream()
                .filter(ur -> !ur.isDeleted() && ur.getRole().getId().equals(roleId))
                .toList();

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("Role assignment not found for this user");
        }

        for (UserRole userRole : matches) {
            userRole.setDeleted(true);
            userRole.setActive(false);
            userRole.setDeletedAt(LocalDateTime.now());
        }

        User saved = userRepository.save(user);

        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("userId", userId.toString());
        oldValue.put("roleId", roleId.toString());
        auditService.record("user_roles", userId, AuditAction.DELETE, oldValue, null);

        return toResponse(userRepository.findByIdWithRoles(saved.getId()).orElse(saved));
    }

    private UserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private UserResponse toResponse(User user) {
        String role = user.getProfileType().name();
        UUID primaryOrgUnitId = user.getPrimaryOrgUnit() != null ? user.getPrimaryOrgUnit().getId() : null;
        List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.isActive() && !ur.isDeleted())
                .map(ur -> ur.getRole().getName().name())
                .distinct()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMobileNumber(),
                user.getDateOfBirth(),
                user.getProfileType(),
                user.getStatus(),
                primaryOrgUnitId,
                user.getPreferredPlatformLanguageId(),
                user.getPreferredAssessmentLanguageId(),
                roles,
                role
        );
    }
}
