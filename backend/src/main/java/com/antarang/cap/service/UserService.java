package com.antarang.cap.service;

import com.antarang.cap.common.PageResponse;
import com.antarang.cap.domain.entity.OrgUnit;
import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.enums.AuditAction;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.domain.enums.UserStatus;
import com.antarang.cap.domain.enums.ProfileRoleMapper;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.request.CreateUserRequest;
import com.antarang.cap.dto.request.UpdateProfileRequest;
import com.antarang.cap.dto.request.UpdateUserLanguageRequest;
import com.antarang.cap.dto.request.UpdateUserRequest;
import com.antarang.cap.dto.request.UpdateUserStatusRequest;
import com.antarang.cap.dto.response.UserResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.OrgUnitRepository;
import com.antarang.cap.repository.RoleRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrgUnitRepository orgUnitRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return toResponse(getCurrentPrincipalUser());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        UserPrincipal principal = getCurrentPrincipal();
        return userRepository.findByTenantIdAndIsDeletedFalse(principal.getTenantId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(
            int page,
            int size,
            ProfileType profileType,
            UUID orgUnitId,
            UserStatus status,
            String search
    ) {
        UserPrincipal principal = getCurrentPrincipal();

        Specification<User> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("tenant").get("id"), principal.getTenantId()),
                cb.isFalse(root.get("isDeleted"))
        );

        if (profileType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("profileType"), profileType));
        }
        if (orgUnitId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("primaryOrgUnit").get("id"), orgUnitId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            ));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.findAll(spec, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        User creator = getCurrentPrincipalUser();

        if (userRepository.existsByTenantIdAndEmailAndIsDeletedFalse(principal.getTenantId(), request.email())) {
            throw new BusinessException("Email is already registered", "EMAIL_ALREADY_EXISTS");
        }

        Role role = roleRepository.findByCode(ProfileRoleMapper.toRoleName(request.profileType()))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setTenant(creator.getTenant());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProfileType(request.profileType());
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMobileNumber(request.mobileNumber());
        user.setDateOfBirth(request.dateOfBirth());
        user.setPreferredPlatformLanguageId(request.preferredPlatformLanguageId());
        user.setPreferredAssessmentLanguageId(request.preferredAssessmentLanguageId());

        if (request.primaryOrgUnitId() != null) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.primaryOrgUnitId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));
            user.setPrimaryOrgUnit(orgUnit);
        }

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRole.setScopeType(request.scopeType() != null ? request.scopeType() : ScopeType.GLOBAL);
        userRole.setScopeId(request.scopeId());
        savedUser.getUserRoles().add(userRole);

        if (request.roleIds() != null) {
            for (UUID roleId : request.roleIds()) {
                if (role.getId().equals(roleId)) {
                    continue;
                }
                Role additionalRole = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
                UserRole additionalUserRole = new UserRole();
                additionalUserRole.setUser(savedUser);
                additionalUserRole.setRole(additionalRole);
                additionalUserRole.setScopeType(ScopeType.GLOBAL);
                savedUser.getUserRoles().add(additionalUserRole);
            }
        }

        return toResponse(userRepository.findByIdWithRoles(savedUser.getId()).orElse(savedUser));
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateProfileRequest request) {
        User user = getCurrentPrincipalUser();
        Map<String, Object> oldValue = snapshotUser(user);

        if (request.email() != null && !request.email().isBlank()
                && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByTenantIdAndEmailAndIsDeletedFalse(user.getTenant().getId(), request.email())) {
                throw new BusinessException("Email is already registered", "EMAIL_ALREADY_EXISTS");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.mobileNumber() != null) {
            user.setMobileNumber(request.mobileNumber());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.preferredPlatformLanguageId() != null) {
            user.setPreferredPlatformLanguageId(request.preferredPlatformLanguageId());
        }
        if (request.preferredAssessmentLanguageId() != null) {
            user.setPreferredAssessmentLanguageId(request.preferredAssessmentLanguageId());
        }

        User saved = userRepository.save(user);
        auditService.record("users", saved.getId(), AuditAction.UPDATE, oldValue, snapshotUser(saved));
        return toResponse(saved);
    }

    private Map<String, Object> snapshotUser(User user) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("email", user.getEmail());
        snapshot.put("firstName", user.getFirstName());
        snapshot.put("lastName", user.getLastName());
        snapshot.put("status", user.getStatus().name());
        snapshot.put("profileType", user.getProfileType().name());
        if (user.getPrimaryOrgUnit() != null) {
            snapshot.put("primaryOrgUnitId", user.getPrimaryOrgUnit().getId().toString());
        }
        return snapshot;
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        Map<String, Object> oldValue = snapshotUser(user);

        if (request.email() != null && !request.email().isBlank()
                && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByTenantIdAndEmailAndIsDeletedFalse(principal.getTenantId(), request.email())) {
                throw new BusinessException("Email is already registered", "EMAIL_ALREADY_EXISTS");
            }
            user.setEmail(request.email());
        }

        if (request.profileType() != null) {
            user.setProfileType(request.profileType());
        }

        if (request.status() != null) {
            user.setStatus(request.status());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.mobileNumber() != null) {
            user.setMobileNumber(request.mobileNumber());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.preferredPlatformLanguageId() != null) {
            user.setPreferredPlatformLanguageId(request.preferredPlatformLanguageId());
        }
        if (request.preferredAssessmentLanguageId() != null) {
            user.setPreferredAssessmentLanguageId(request.preferredAssessmentLanguageId());
        }

        if (request.primaryOrgUnitId() != null) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.primaryOrgUnitId())
                    .filter(found -> !found.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));
            if (!orgUnit.getTenant().getId().equals(principal.getTenantId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
            user.setPrimaryOrgUnit(orgUnit);
        }

        User saved = userRepository.save(user);
        auditService.record("users", saved.getId(), AuditAction.UPDATE, oldValue, snapshotUser(saved));
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        Map<String, Object> oldValue = Map.of("status", user.getStatus().name());
        user.setStatus(request.status());
        User saved = userRepository.save(user);
        auditService.record("users", saved.getId(), AuditAction.STATUS_CHANGE, oldValue, Map.of("status", saved.getStatus().name()));
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updatePlatformLanguage(UUID id, UpdateUserLanguageRequest request) {
        return updateLanguage(id, request.languageId(), true);
    }

    @Transactional
    public UserResponse updateAssessmentLanguage(UUID id, UpdateUserLanguageRequest request) {
        return updateLanguage(id, request.languageId(), false);
    }

    private UserResponse updateLanguage(UUID id, UUID languageId, boolean platform) {
        UserPrincipal principal = getCurrentPrincipal();
        User user = userRepository.findByIdWithRoles(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(principal.getTenantId()) && !id.equals(principal.getId())) {
            throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
        }

        if (platform) {
            user.setPreferredPlatformLanguageId(languageId);
        } else {
            user.setPreferredAssessmentLanguageId(languageId);
        }

        return toResponse(userRepository.save(user));
    }

    private User getCurrentPrincipalUser() {
        UserPrincipal principal = getCurrentPrincipal();
        return userRepository.findByIdWithRoles(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
