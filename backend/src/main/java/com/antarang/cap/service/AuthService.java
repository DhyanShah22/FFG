package com.antarang.cap.service;

import com.antarang.cap.config.JwtProperties;
import com.antarang.cap.domain.entity.ConsentRecord;
import com.antarang.cap.domain.entity.LoginHistory;
import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.domain.entity.OrgUnit;
import com.antarang.cap.domain.entity.Permission;
import com.antarang.cap.domain.entity.RefreshToken;
import com.antarang.cap.domain.entity.Role;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.entity.UserProfile;
import com.antarang.cap.domain.entity.UserRole;
import com.antarang.cap.domain.entity.SignupSession;
import com.antarang.cap.repository.LoginHistoryRepository;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.domain.enums.UserStatus;
import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.dto.request.ForgotPasswordRequest;
import com.antarang.cap.dto.request.LoginRequest;
import com.antarang.cap.dto.request.RefreshTokenRequest;
import com.antarang.cap.dto.request.RegisterRequest;
import com.antarang.cap.dto.request.ResendPasswordResetOtpRequest;
import com.antarang.cap.dto.request.ResetPasswordRequest;
import com.antarang.cap.dto.response.AuthMeResponse;
import com.antarang.cap.dto.response.AuthUserSummary;
import com.antarang.cap.dto.response.LoginResponse;
import com.antarang.cap.dto.response.OtpChallengeResponse;
import com.antarang.cap.dto.response.RegisterResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.domain.enums.ProfileRoleMapper;
import com.antarang.cap.repository.ConsentRecordRepository;
import com.antarang.cap.repository.LanguageRepository;
import com.antarang.cap.repository.OrgUnitRepository;
import com.antarang.cap.repository.RefreshTokenRepository;
import com.antarang.cap.repository.RoleRepository;
import com.antarang.cap.repository.TenantRepository;
import com.antarang.cap.repository.UserProfileRepository;
import com.antarang.cap.repository.UserRepository;
import com.antarang.cap.security.JwtService;
import com.antarang.cap.security.TokenDenylistService;
import com.antarang.cap.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final TokenDenylistService tokenDenylistService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final LanguageRepository languageRepository;
    private final UserProfileRepository userProfileRepository;
    private final RegisterSignupValidator registerSignupValidator;
    private final RegisterSignupProfileMapper registerSignupProfileMapper;
    private final ConsentRecordRepository consentRecordRepository;
    private final OtpService otpService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            LoginHistoryRepository loginHistoryRepository,
            JwtService jwtService,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            TokenDenylistService tokenDenylistService,
            RefreshTokenRepository refreshTokenRepository,
            OrgUnitRepository orgUnitRepository,
            LanguageRepository languageRepository,
            UserProfileRepository userProfileRepository,
            RegisterSignupValidator registerSignupValidator,
            RegisterSignupProfileMapper registerSignupProfileMapper,
            ConsentRecordRepository consentRecordRepository,
            OtpService otpService,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.tokenDenylistService = tokenDenylistService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.languageRepository = languageRepository;
        this.userProfileRepository = userProfileRepository;
        this.registerSignupValidator = registerSignupValidator;
        this.registerSignupProfileMapper = registerSignupProfileMapper;
        this.consentRecordRepository = consentRecordRepository;
        this.otpService = otpService;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String loginId = request.resolvedLoginId();
        if (loginId == null || loginId.isBlank() || request.password() == null || request.password().isBlank()) {
            throw new BusinessException("loginId/email and password are required", "VALIDATION_ERROR");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, request.password())
            );

            User user = userRepository.findByLoginIdWithRoles(loginId)
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            if (request.profileType() != null && request.profileType() != user.getProfileType()) {
                logAttempt(user.getId(), httpRequest, "FAILED", "Incorrect account profile");
                throw new BusinessException("The selected account profile does not match this user", "PROFILE_MISMATCH");
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                logAttempt(user.getId(), httpRequest, "FAILED", "User inactive");
                throw new BusinessException("User account is not active", "USER_INACTIVE");
            }

            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            UserPrincipal principal = UserPrincipal.from(user);
            logAttempt(user.getId(), httpRequest, "SUCCESS", null);
            activityLogService.logLogin(user.getId(), user.getTenant().getId(), "password");
            return issueTokens(user, principal);
        } catch (BadCredentialsException ex) {
            UUID userId = userRepository.findByLoginIdWithRoles(loginId).map(User::getId).orElse(null);
            logAttempt(userId, httpRequest, "FAILED", "Invalid credentials");
            throw ex;
        }
    }

    @Transactional
    public LoginResponse loginAuthenticatedUser(User user, UserPrincipal principal, HttpServletRequest httpRequest) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            logAttempt(user.getId(), httpRequest, "FAILED", "User inactive");
            throw new BusinessException("User account is not active", "USER_INACTIVE");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        logAttempt(user.getId(), httpRequest, "SUCCESS", null);
        activityLogService.logLogin(user.getId(), user.getTenant().getId(), "authenticated");
        return issueTokens(user, principal);
    }

    @Transactional
    public RegisterResponse registerFromSession(
            RegisterRequest request,
            SignupSession session,
            HttpServletRequest httpRequest
    ) {
        Tenant tenant = session.getTenant();
        ProfileType profileType = session.getProfileType();
        registerSignupValidator.validate(request);

        String email = resolveRegistrationEmail(request, tenant);
        if (userRepository.existsByTenantIdAndEmailAndIsDeletedFalse(tenant.getId(), email)) {
            throw new BusinessException("A profile with this email already exists. Please log in or reset your password.", "DUPLICATE_RESOURCE");
        }
        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()
                && userRepository.existsByTenantIdAndMobileNumberAndIsDeletedFalse(tenant.getId(), request.mobileNumber())) {
            throw new BusinessException("A profile with this mobile number already exists. Please log in or reset your password.", "DUPLICATE_RESOURCE");
        }
        if (request.username() != null && !request.username().isBlank()
                && userRepository.existsByTenantIdAndUsernameAndIsDeletedFalse(tenant.getId(), request.username().trim())) {
            throw new BusinessException("Username is already taken", "DUPLICATE_RESOURCE");
        }

        Role role = roleRepository.findByCode(ProfileRoleMapper.toRoleName(profileType))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(email);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProfileType(profileType);
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName(request.firstName());
        user.setMiddleName(request.middleName());
        user.setLastName(request.lastName());
        user.setMobileNumber(request.mobileNumber());
        user.setDateOfBirth(request.dateOfBirth());
        user.setGenderConfigId(request.genderConfigId());
        user.setPreferredPlatformLanguageId(request.preferredPlatformLanguageId());
        user.setPreferredAssessmentLanguageId(request.preferredAssessmentLanguageId());

        if (request.primaryOrgUnitId() != null) {
            OrgUnit orgUnit = orgUnitRepository.findById(request.primaryOrgUnitId())
                    .filter(o -> !o.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Org unit not found"));
            if (!orgUnit.getTenant().getId().equals(tenant.getId())) {
                throw new BusinessException("Cross-tenant access is not allowed", "ACCESS_DENIED");
            }
            user.setPrimaryOrgUnit(orgUnit);
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setScopeType(ScopeType.GLOBAL);
        user.getUserRoles().add(userRole);

        UserProfile userProfile = registerSignupProfileMapper.buildProfile(user, request);
        userRepository.save(user);
        userProfileRepository.save(userProfile);

        ConsentRecord consentRecord = new ConsentRecord();
        consentRecord.setUser(user);
        consentRecord.setConsentType(request.consent().consentType());
        consentRecord.setConsentTextVersion(request.consent().consentTextVersion());
        consentRecord.setGuardianName(request.consent().guardianName());
        consentRecord.setGuardianContact(request.consent().guardianContact());
        consentRecord.setConsentGiven(true);
        consentRecord.setConsentGivenAt(Instant.now());
        consentRecordRepository.save(consentRecord);

        logAttempt(user.getId(), httpRequest, "SUCCESS", "Registration");

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "User");
        variables.put("loginUrl", "https://cap.antarang.org");
        notificationService.enqueue(user.getId(), "ACCOUNT_CREATED", variables);

        boolean consentRequired = requiresGuardianConsent(request.dateOfBirth());
        return new RegisterResponse(
                user.getId(),
                user.getProfileType(),
                user.getStatus(),
                consentRequired,
                consentRequired ? "GUARDIAN" : "SELF"
        );
    }

    private String resolveRegistrationEmail(RegisterRequest request, Tenant tenant) {
        if (request.email() != null && !request.email().isBlank()) {
            return request.email().trim().toLowerCase();
        }
        if (request.username() != null && !request.username().isBlank()) {
            return request.username().trim().toLowerCase() + "@username." + tenant.getCode() + ".local";
        }
        throw new BusinessException("Email or username is required", "VALIDATION_ERROR");
    }

    @Transactional(readOnly = true)
    public AuthMeResponse me() {
        UserPrincipal principal = currentPrincipal();
        User user = userRepository.findByIdWithRoles(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toMeResponse(user);
    }

    public void logout(String accessToken, String refreshToken) {
        UUID userId = null;
        UUID tenantId = null;
        if (accessToken != null && jwtService.isTokenValid(accessToken)) {
            userId = jwtService.extractUserId(accessToken);
            tenantId = jwtService.extractTenantId(accessToken);
            tokenDenylistService.revoke(jwtService.extractJti(accessToken), jwtService.extractExpiration(accessToken));
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            revokeRefreshToken(refreshToken);
            if (jwtService.isTokenValid(refreshToken)) {
                if (userId == null) {
                    userId = jwtService.extractUserId(refreshToken);
                }
                if (tenantId == null) {
                    tenantId = jwtService.extractTenantId(refreshToken);
                }
                tokenDenylistService.revoke(jwtService.extractJti(refreshToken), jwtService.extractExpiration(refreshToken));
            }
        }
        if (userId != null && tenantId != null) {
            activityLogService.logLogout(userId, tenantId);
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isTokenValid(refreshToken) || !"refresh".equals(jwtService.extractTokenType(refreshToken))) {
            throw new BusinessException("Invalid or expired refresh token", "AUTH_TOKEN_INVALID");
        }

        String hash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("Invalid or expired refresh token", "AUTH_TOKEN_INVALID"));

        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token is no longer valid", "AUTH_TOKEN_INVALID");
        }

        if (tokenDenylistService.isRevoked(jwtService.extractJti(refreshToken))) {
            throw new BusinessException("Refresh token is no longer valid", "AUTH_TOKEN_INVALID");
        }

        User user = userRepository.findByIdWithRoles(stored.getUser().getId())
                .orElseThrow(() -> new BusinessException("Invalid or expired refresh token", "AUTH_TOKEN_INVALID"));

        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException("User account is not active", "USER_INACTIVE");
        }

        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);
        tokenDenylistService.revoke(jwtService.extractJti(refreshToken), jwtService.extractExpiration(refreshToken));

        return issueTokens(user, UserPrincipal.from(user));
    }

    @Transactional
    public OtpChallengeResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        Optional<User> maybeUser = resolveUserForPasswordReset(request);
        if (maybeUser.isEmpty()) {
            log.info("Password reset requested for unknown account; no OTP issued.");
            return null;
        }

        User user = maybeUser.get();
        OtpChannel channel = resolvePasswordResetChannel(request, user);
        String destination = channel == OtpChannel.EMAIL ? user.getEmail() : user.getMobileNumber();
        if (destination == null || destination.isBlank()) {
            throw new BusinessException("No destination available for the selected channel", "VALIDATION_ERROR");
        }

        log.info("Password reset OTP requested for user '{}' via {}", user.getEmail(), channel);
        OtpChallengeResponse response = otpService.sendPasswordResetOtp(user, channel, destination);
        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "User");
        notificationService.enqueue(user.getId(), "PASSWORD_RESET", variables);
        return response;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var challenge = otpService.verifyPasswordResetOtp(request.challengeId(), request.otp());
        User user = challenge.getUser();
        if (user == null) {
            throw new BusinessException("Invalid OTP challenge", "OTP_INVALID");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password successfully reset for user '{}' via OTP.", user.getEmail());

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "User");
        notificationService.enqueue(user.getId(), "PASSWORD_RESET", variables);
    }

    private Optional<User> resolveUserForPasswordReset(ForgotPasswordRequest request) {
        if (request.email() != null && !request.email().isBlank()) {
            return userRepository.findByLoginIdWithRoles(request.email().trim().toLowerCase());
        }
        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()) {
            List<User> users = userRepository.findByMobileNumberWithRoles(request.mobileNumber().trim());
            if (request.profileType() != null) {
                users = users.stream()
                        .filter(u -> u.getProfileType() == request.profileType())
                        .toList();
            }
            if (users.isEmpty()) {
                return Optional.empty();
            }
            if (users.size() > 1) {
                throw new BusinessException(
                        "Multiple accounts match this mobile number; specify profileType",
                        "AMBIGUOUS_USER"
                );
            }
            return Optional.of(users.get(0));
        }
        throw new BusinessException("email or mobileNumber is required", "VALIDATION_ERROR");
    }

    private OtpChannel resolvePasswordResetChannel(ForgotPasswordRequest request, User user) {
        if (request.channel() != null) {
            return request.channel();
        }
        if (request.email() != null && !request.email().isBlank()) {
            return OtpChannel.EMAIL;
        }
        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()) {
            if (user.getProfileType() != ProfileType.CAREER_EXPLORER) {
                throw new BusinessException("Mobile password reset is only available for Career Explorer", "VALIDATION_ERROR");
            }
            return OtpChannel.MOBILE;
        }
        throw new BusinessException("email or mobileNumber is required", "VALIDATION_ERROR");
    }

    @Transactional
    public OtpChallengeResponse resendPasswordResetOtp(ResendPasswordResetOtpRequest request) {
        return otpService.resendPasswordResetOtp(request.challengeId());
    }

    private LoginResponse issueTokens(User user, UserPrincipal principal) {
        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hashToken(refresh));
        entity.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()));
        refreshTokenRepository.save(entity);

        AuthUserSummary summary = toUserSummary(user);
        return new LoginResponse(
                access,
                refresh,
                jwtProperties.getAccessTokenExpirationMs() / 1000,
                summary,
                principal.getPrimaryRole(),
                principal.getPrimaryOrgUnitId()
        );
    }

    private void revokeRefreshToken(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private Tenant resolveTenant(UUID tenantId, String tenantCode) {
        if (tenantId != null) {
            return tenantRepository.findById(tenantId)
                    .filter(t -> !t.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        }
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new BusinessException("tenantId or tenantCode is required", "VALIDATION_ERROR");
        }
        return tenantRepository.findByCodeAndIsDeletedFalse(tenantCode)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    }

    private boolean requiresGuardianConsent(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
    }

    private AuthUserSummary toUserSummary(User user) {
        List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.isActive() && !ur.isDeleted())
                .map(ur -> ur.getRole().getName().name())
                .distinct()
                .toList();
        return new AuthUserSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileType(),
                user.getStatus(),
                roles,
                user.getTenant().getId(),
                user.getPrimaryOrgUnit() != null ? user.getPrimaryOrgUnit().getId() : null,
                languageCode(user.getPreferredPlatformLanguageId()),
                languageCode(user.getPreferredAssessmentLanguageId())
        );
    }

    private AuthMeResponse toMeResponse(User user) {
        List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.isActive() && !ur.isDeleted())
                .map(ur -> ur.getRole().getName().name())
                .distinct()
                .toList();
        List<String> permissions = user.getUserRoles().stream()
                .filter(ur -> ur.isActive() && !ur.isDeleted())
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return new AuthMeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getProfileType(),
                user.getStatus(),
                roles,
                permissions,
                user.getTenant().getId(),
                user.getPrimaryOrgUnit() != null ? user.getPrimaryOrgUnit().getId() : null,
                user.getPreferredPlatformLanguageId(),
                user.getPreferredAssessmentLanguageId()
        );
    }

    private String languageCode(UUID languageId) {
        if (languageId == null) {
            return null;
        }
        return languageRepository.findById(languageId).map(Language::getCode).orElse(null);
    }

    private UserPrincipal currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authentication required", "UNAUTHORIZED");
        }
        return principal;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private void logAttempt(
            UUID userId,
            HttpServletRequest httpRequest,
            String loginStatus,
            String failureReason
    ) {
        LoginHistory entry = new LoginHistory();
        entry.setUserId(userId);
        entry.setIpAddress(httpRequest.getRemoteAddr());
        entry.setUserAgent(httpRequest.getHeader("User-Agent"));
        entry.setLoginStatus(loginStatus);
        entry.setFailureReason(failureReason);
        loginHistoryRepository.save(entry);
    }
}
