package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.VerificationToken;
import com.antarang.cap.domain.enums.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenHashAndTokenType(String tokenHash, VerificationTokenType tokenType);
}
