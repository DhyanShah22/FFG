package com.antarang.cap.service;

import com.antarang.cap.config.OtpProperties;
import com.antarang.cap.domain.entity.SignupSession;
import com.antarang.cap.domain.entity.Tenant;
import com.antarang.cap.domain.enums.CommunicationChannel;
import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.request.CreateSignupSessionRequest;
import com.antarang.cap.dto.request.RegisterRequest;
import com.antarang.cap.dto.request.SendOtpRequest;
import com.antarang.cap.dto.request.UpdateSignupSessionRequest;
import com.antarang.cap.dto.request.VerifyOtpRequest;
import com.antarang.cap.dto.response.OtpChallengeResponse;
import com.antarang.cap.dto.response.RegisterResponse;
import com.antarang.cap.dto.response.SignupSessionResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.exception.ResourceNotFoundException;
import com.antarang.cap.repository.SignupSessionRepository;
import com.antarang.cap.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SignupSessionService {

    private final SignupSessionRepository signupSessionRepository;
    private final TenantRepository tenantRepository;
    private final OtpService otpService;
    private final OtpProperties otpProperties;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public SignupSessionService(
            SignupSessionRepository signupSessionRepository,
            TenantRepository tenantRepository,
            OtpService otpService,
            OtpProperties otpProperties,
            AuthService authService,
            ObjectMapper objectMapper
    ) {
        this.signupSessionRepository = signupSessionRepository;
        this.tenantRepository = tenantRepository;
        this.otpService = otpService;
        this.otpProperties = otpProperties;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SignupSessionResponse createSession(CreateSignupSessionRequest request) {
        Tenant tenant = resolveTenant(request.tenantId(), request.tenantCode());
        Instant now = Instant.now();

        SignupSession session = new SignupSession();
        session.setTenant(tenant);
        session.setProfileType(request.profileType());
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plus(Duration.ofHours(otpProperties.getSignupSessionHours())));
        session.setSessionData(new HashMap<>());

        SignupSession saved = signupSessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional
    public SignupSessionResponse updateSession(UUID sessionId, UpdateSignupSessionRequest request) {
        SignupSession session = requireActiveSession(sessionId);
        if (request.sessionData() != null) {
            Map<String, Object> merged = new HashMap<>(session.getSessionData());
            merged.putAll(request.sessionData());
            applyBackNavigationVerificationRules(session, merged);
            session.setSessionData(merged);
        }
        touchSession(session);
        return toResponse(signupSessionRepository.save(session));
    }

    @Transactional
    public void abandonSession(UUID sessionId) {
        SignupSession session = signupSessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Signup session not found or already completed"));

        otpService.supersedeAllSignupOtps(session);
        session.setEmailVerified(false);
        session.setMobileVerified(false);
        session.setSessionData(new HashMap<>());
        Instant now = Instant.now();
        session.setExpiresAt(now);
        session.setCompletedAt(now);
        signupSessionRepository.save(session);
    }

    @Transactional
    public void expireSignupOtp(UUID sessionId, OtpChannel channel) {
        SignupSession session = requireActiveSession(sessionId);
        otpService.expireSignupOtp(session, channel);
        if (channel == OtpChannel.EMAIL) {
            session.setEmailVerified(false);
            session.getSessionData().remove(SignupSessionMetadata.VERIFIED_EMAIL);
        } else {
            session.setMobileVerified(false);
            session.getSessionData().remove(SignupSessionMetadata.VERIFIED_MOBILE);
        }
        signupSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public SignupSessionResponse getSession(UUID sessionId) {
        SignupSession session = signupSessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Signup session not found or already completed"));
        if (session.isExpired()) {
            throw new BusinessException("Signup session has expired", "SIGNUP_SESSION_EXPIRED");
        }
        return toResponse(session);
    }

    @Transactional
    public OtpChallengeResponse sendOtp(UUID sessionId, SendOtpRequest request) {
        SignupSession session = requireActiveSession(sessionId);
        String destination = resolveSignupDestination(session, request.channel(), request.destination());
        return otpService.sendSignupOtp(session, request.channel(), destination);
    }

    @Transactional
    public OtpChallengeResponse resendOtp(UUID sessionId, OtpChannel channel) {
        SignupSession session = requireActiveSession(sessionId);
        return otpService.resendSignupOtp(session, channel);
    }

    @Transactional
    public SignupSessionResponse verifyOtp(UUID sessionId, VerifyOtpRequest request) {
        SignupSession session = requireActiveSession(sessionId);
        otpService.verifySignupOtp(session, request.channel(), request.otp());
        return toResponse(signupSessionRepository.save(session));
    }

    @Transactional
    public RegisterResponse completeSession(UUID sessionId, HttpServletRequest httpRequest) {
        SignupSession session = requireActiveSession(sessionId);
        validateOtpVerification(session);
        RegisterRequest registerRequest = toRegisterRequest(session);
        RegisterResponse response = authService.registerFromSession(registerRequest, session, httpRequest);
        session.setCompletedAt(Instant.now());
        signupSessionRepository.save(session);
        return response;
    }

    private void validateOtpVerification(SignupSession session) {
        ProfileType profileType = session.getProfileType();
        if (profileType == ProfileType.CAREER_EXPLORER) {
            List<CommunicationChannel> channels = readCommunicationChannels(session.getSessionData());
            if (channels.isEmpty()) {
                throw new BusinessException("Preferred communication channel is required", "VALIDATION_ERROR");
            }
            boolean needsEmail = channels.contains(CommunicationChannel.EMAIL)
                    || channels.contains(CommunicationChannel.EMAIL_AND_MOBILE);
            boolean needsMobile = channels.contains(CommunicationChannel.MOBILE)
                    || channels.contains(CommunicationChannel.EMAIL_AND_MOBILE);
            if (needsEmail && !session.isEmailVerified()) {
                throw new BusinessException("Email must be verified before completing sign-up", "OTP_REQUIRED");
            }
            if (needsMobile && !session.isMobileVerified()) {
                throw new BusinessException("Mobile number must be verified before completing sign-up", "OTP_REQUIRED");
            }
        } else {
            if (!session.isEmailVerified()) {
                throw new BusinessException("Email must be verified before completing sign-up", "OTP_REQUIRED");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<CommunicationChannel> readCommunicationChannels(Map<String, Object> sessionData) {
        Object signupDetails = sessionData.get("signupDetails");
        if (signupDetails instanceof Map<?, ?> details) {
            Object careerExplorer = details.get("careerExplorer");
            if (careerExplorer instanceof Map<?, ?> ce) {
                Object channels = ce.get("preferredCommunicationChannels");
                if (channels instanceof List<?> list) {
                    return list.stream()
                            .map(Object::toString)
                            .map(CommunicationChannel::valueOf)
                            .toList();
                }
            }
        }
        return List.of();
    }

    private RegisterRequest toRegisterRequest(SignupSession session) {
        Map<String, Object> data = new HashMap<>(session.getSessionData());
        data.keySet().removeIf(SignupSessionMetadata::isMetadataKey);
        data.put("tenantId", session.getTenant().getId());
        data.put("profileType", session.getProfileType().name());
        return objectMapper.convertValue(data, RegisterRequest.class);
    }

    /**
     * IF2AF0203 §8.A — if email/mobile changes after OTP verification, require re-verification.
     */
    private void applyBackNavigationVerificationRules(SignupSession session, Map<String, Object> merged) {
        String newEmail = normalizeEmail(merged.get("email"));
        String newMobile = normalizeMobile(merged.get("mobileNumber"));

        if (session.isEmailVerified()) {
            String verifiedEmail = normalizeEmail(merged.get(SignupSessionMetadata.VERIFIED_EMAIL));
            if (verifiedEmail != null && newEmail != null && !verifiedEmail.equals(newEmail)) {
                session.setEmailVerified(false);
                merged.remove(SignupSessionMetadata.VERIFIED_EMAIL);
                otpService.supersedeSignupOtp(session, OtpChannel.EMAIL);
            }
        }

        if (session.isMobileVerified()) {
            String verifiedMobile = normalizeMobile(merged.get(SignupSessionMetadata.VERIFIED_MOBILE));
            if (verifiedMobile != null && newMobile != null && !verifiedMobile.equals(newMobile)) {
                session.setMobileVerified(false);
                merged.remove(SignupSessionMetadata.VERIFIED_MOBILE);
                otpService.supersedeSignupOtp(session, OtpChannel.MOBILE);
            }
        }
    }

    private String normalizeEmail(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return value.toString().trim().toLowerCase();
    }

    private String normalizeMobile(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return value.toString().trim();
    }

    private String resolveSignupDestination(SignupSession session, OtpChannel channel, String destination) {
        if (destination != null && !destination.isBlank()) {
            return destination;
        }
        Map<String, Object> data = session.getSessionData();
        if (channel == OtpChannel.EMAIL) {
            Object email = data.get("email");
            if (email != null && !email.toString().isBlank()) {
                return email.toString();
            }
        } else {
            Object mobile = data.get("mobileNumber");
            if (mobile != null && !mobile.toString().isBlank()) {
                return mobile.toString();
            }
        }
        throw new BusinessException("Destination not found in session data", "VALIDATION_ERROR");
    }

    private SignupSession requireActiveSession(UUID sessionId) {
        SignupSession session = signupSessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Signup session not found or already completed"));
        if (session.isExpired()) {
            throw new BusinessException("Signup session has expired", "SIGNUP_SESSION_EXPIRED");
        }
        return session;
    }

    private void touchSession(SignupSession session) {
        Instant now = Instant.now();
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plus(Duration.ofHours(otpProperties.getSignupSessionHours())));
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

    private SignupSessionResponse toResponse(SignupSession session) {
        Map<String, Object> publicData = new HashMap<>(session.getSessionData());
        publicData.keySet().removeIf(SignupSessionMetadata::isMetadataKey);
        return new SignupSessionResponse(
                session.getId(),
                session.getTenant().getId(),
                session.getProfileType(),
                session.isEmailVerified(),
                session.isMobileVerified(),
                session.getExpiresAt(),
                session.getLastActivityAt(),
                publicData
        );
    }
}
