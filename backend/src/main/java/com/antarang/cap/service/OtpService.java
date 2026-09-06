package com.antarang.cap.service;

import com.antarang.cap.config.OtpProperties;
import com.antarang.cap.domain.entity.OtpChallenge;
import com.antarang.cap.domain.entity.SignupSession;
import com.antarang.cap.domain.entity.User;
import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.domain.enums.OtpPurpose;
import com.antarang.cap.dto.response.OtpChallengeResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.OtpChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpChallengeRepository otpChallengeRepository;
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpChallengeRepository otpChallengeRepository, OtpProperties otpProperties) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.otpProperties = otpProperties;
    }

    @Transactional
    public OtpChallengeResponse sendSignupOtp(SignupSession session, OtpChannel channel, String destination) {
        touchSession(session);
        OtpPurpose purpose = channel == OtpChannel.EMAIL ? OtpPurpose.SIGNUP_EMAIL : OtpPurpose.SIGNUP_MOBILE;
        return createChallenge(session, null, purpose, channel, destination);
    }

    @Transactional
    public OtpChallengeResponse sendPasswordResetOtp(User user, OtpChannel channel, String destination) {
        OtpPurpose purpose = channel == OtpChannel.EMAIL ? OtpPurpose.PASSWORD_RESET_EMAIL : OtpPurpose.PASSWORD_RESET_MOBILE;
        return createChallenge(null, user, purpose, channel, destination);
    }

    @Transactional
    public OtpChallenge verifySignupOtp(SignupSession session, OtpChannel channel, String otp) {
        OtpPurpose purpose = channel == OtpChannel.EMAIL ? OtpPurpose.SIGNUP_EMAIL : OtpPurpose.SIGNUP_MOBILE;
        OtpChallenge challenge = verifyChallenge(session, null, purpose, channel, otp);
        if (channel == OtpChannel.EMAIL) {
            session.setEmailVerified(true);
            session.getSessionData().put(SignupSessionMetadata.VERIFIED_EMAIL, challenge.getDestination());
        } else {
            session.setMobileVerified(true);
            session.getSessionData().put(SignupSessionMetadata.VERIFIED_MOBILE, challenge.getDestination());
        }
        touchSession(session);
        return challenge;
    }

    @Transactional
    public OtpChallenge verifyPasswordResetOtp(UUID challengeId, String otp) {
        OtpChallenge challenge = otpChallengeRepository.findActiveById(challengeId)
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP challenge", "OTP_INVALID"));

        if (challenge.getPurpose() != OtpPurpose.PASSWORD_RESET_EMAIL
                && challenge.getPurpose() != OtpPurpose.PASSWORD_RESET_MOBILE) {
            throw new BusinessException("Invalid OTP challenge", "OTP_INVALID");
        }
        if (!challenge.isActive()) {
            throw new BusinessException("OTP has expired", "OTP_EXPIRED");
        }
        if (!matchesOtp(otp, challenge.getOtpHash())) {
            throw new BusinessException("Incorrect OTP", "OTP_INCORRECT");
        }
        challenge.setVerifiedAt(Instant.now());
        otpChallengeRepository.save(challenge);
        return challenge;
    }

    @Transactional
    public OtpChallengeResponse resendSignupOtp(SignupSession session, OtpChannel channel) {
        List<OtpPurpose> purposes = channel == OtpChannel.EMAIL
                ? List.of(OtpPurpose.SIGNUP_EMAIL)
                : List.of(OtpPurpose.SIGNUP_MOBILE);

        OtpChallenge existing = otpChallengeRepository
                .findActiveSignupChallenges(session.getId(), channel, purposes)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("No active OTP to resend. Send OTP first.", "OTP_NOT_FOUND"));

        if (Instant.now().isBefore(existing.getResendAvailableAt())) {
            long wait = Duration.between(Instant.now(), existing.getResendAvailableAt()).getSeconds();
            throw new BusinessException("Resend OTP available in " + wait + " seconds", "OTP_RESEND_TOO_EARLY");
        }

        existing.setSupersededAt(Instant.now());
        otpChallengeRepository.save(existing);

        return sendSignupOtp(session, channel, existing.getDestination());
    }

    @Transactional
    public OtpChallengeResponse resendPasswordResetOtp(UUID challengeId) {
        OtpChallenge existing = otpChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException("Invalid or expired OTP challenge", "OTP_INVALID"));

        if (existing.getPurpose() != OtpPurpose.PASSWORD_RESET_EMAIL
                && existing.getPurpose() != OtpPurpose.PASSWORD_RESET_MOBILE) {
            throw new BusinessException("Invalid OTP challenge", "OTP_INVALID");
        }
        if (existing.getUser() == null) {
            throw new BusinessException("Invalid OTP challenge", "OTP_INVALID");
        }

        if (existing.getSupersededAt() == null && existing.getVerifiedAt() == null
                && Instant.now().isBefore(existing.getResendAvailableAt())) {
            long wait = Duration.between(Instant.now(), existing.getResendAvailableAt()).getSeconds();
            throw new BusinessException("Resend OTP available in " + wait + " seconds", "OTP_RESEND_TOO_EARLY");
        }

        if (existing.getSupersededAt() == null && existing.getVerifiedAt() == null) {
            existing.setSupersededAt(Instant.now());
            otpChallengeRepository.save(existing);
        }

        return sendPasswordResetOtp(
                existing.getUser(),
                existing.getChannel(),
                existing.getDestination()
        );
    }

    @Transactional
    public void expireSignupOtp(SignupSession session, OtpChannel channel) {
        supersedeSignupOtp(session, channel);
        touchSession(session);
    }

    @Transactional
    public void supersedeAllSignupOtps(SignupSession session) {
        Instant now = Instant.now();
        for (OtpChallenge challenge : otpChallengeRepository.findAllActiveSignupChallenges(session.getId())) {
            challenge.setSupersededAt(now);
            otpChallengeRepository.save(challenge);
        }
    }

    @Transactional
    public void supersedeSignupOtp(SignupSession session, OtpChannel channel) {
        List<OtpPurpose> purposes = channel == OtpChannel.EMAIL
                ? List.of(OtpPurpose.SIGNUP_EMAIL)
                : List.of(OtpPurpose.SIGNUP_MOBILE);
        Instant now = Instant.now();
        for (OtpChallenge challenge : otpChallengeRepository.findActiveSignupChallenges(
                session.getId(), channel, purposes)) {
            challenge.setSupersededAt(now);
            otpChallengeRepository.save(challenge);
        }
    }

    private OtpChallengeResponse createChallenge(
            SignupSession session,
            User user,
            OtpPurpose purpose,
            OtpChannel channel,
            String destination
    ) {
        String normalizedDestination = normalizeDestination(channel, destination);
        String rawOtp = generateFourDigitOtp();
        Instant now = Instant.now();

        OtpChallenge challenge = new OtpChallenge();
        challenge.setSignupSession(session);
        challenge.setUser(user);
        challenge.setPurpose(purpose);
        challenge.setChannel(channel);
        challenge.setDestination(normalizedDestination);
        challenge.setOtpHash(hashOtp(rawOtp));
        challenge.setExpiresAt(now.plusSeconds(otpProperties.getValiditySeconds()));
        challenge.setResendAvailableAt(now.plusSeconds(otpProperties.getResendDelaySeconds()));
        otpChallengeRepository.save(challenge);

        log.info("[SIMULATED OTP] channel={} destination={} otp={} (valid {}s, resend after {}s)",
                channel, normalizedDestination, rawOtp,
                otpProperties.getValiditySeconds(), otpProperties.getResendDelaySeconds());

        return new OtpChallengeResponse(
                challenge.getId(),
                channel,
                normalizedDestination,
                otpProperties.getValiditySeconds(),
                otpProperties.getResendDelaySeconds(),
                rawOtp
        );
    }

    private OtpChallenge verifyChallenge(
            SignupSession session,
            User user,
            OtpPurpose purpose,
            OtpChannel channel,
            String otp
    ) {
        if (session != null && session.isExpired()) {
            throw new BusinessException("Signup session has expired", "SIGNUP_SESSION_EXPIRED");
        }

        OtpChallenge challenge;
        if (session != null) {
            challenge = otpChallengeRepository
                    .findActiveSignupChallenges(session.getId(), channel, List.of(purpose))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("No active OTP for this channel", "OTP_NOT_FOUND"));
        } else {
            throw new BusinessException("OTP context not found", "OTP_INVALID");
        }

        if (!challenge.isActive()) {
            throw new BusinessException("OTP has expired", "OTP_EXPIRED");
        }
        if (!matchesOtp(otp, challenge.getOtpHash())) {
            throw new BusinessException("Incorrect OTP", "OTP_INCORRECT");
        }

        challenge.setVerifiedAt(Instant.now());
        otpChallengeRepository.save(challenge);
        return challenge;
    }

    private void touchSession(SignupSession session) {
        Instant now = Instant.now();
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plus(Duration.ofHours(otpProperties.getSignupSessionHours())));
    }

    private String generateFourDigitOtp() {
        int value = secureRandom.nextInt(10000);
        return String.format("%04d", value);
    }

    private boolean matchesOtp(String rawOtp, String hash) {
        return hashOtp(rawOtp).equals(hash);
    }

    private String hashOtp(String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawOtp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String normalizeDestination(OtpChannel channel, String destination) {
        if (destination == null || destination.isBlank()) {
            throw new BusinessException("Destination is required", "VALIDATION_ERROR");
        }
        if (channel == OtpChannel.EMAIL) {
            return destination.trim().toLowerCase();
        }
        return destination.trim();
    }
}
