package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.OtpChallenge;
import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.domain.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    @Query("""
            SELECT c FROM OtpChallenge c
            WHERE c.id = :id
            AND c.supersededAt IS NULL
            AND c.verifiedAt IS NULL
            """)
    Optional<OtpChallenge> findActiveById(@Param("id") UUID id);

    @Query("""
            SELECT c FROM OtpChallenge c
            WHERE c.signupSession.id = :sessionId
            AND c.channel = :channel
            AND c.purpose IN :purposes
            AND c.supersededAt IS NULL
            AND c.verifiedAt IS NULL
            ORDER BY c.createdAt DESC
            """)
    List<OtpChallenge> findActiveSignupChallenges(
            @Param("sessionId") UUID sessionId,
            @Param("channel") OtpChannel channel,
            @Param("purposes") java.util.List<OtpPurpose> purposes
    );

    @Query("""
            SELECT c FROM OtpChallenge c
            WHERE c.signupSession.id = :sessionId
            AND c.supersededAt IS NULL
            AND c.verifiedAt IS NULL
            """)
    List<OtpChallenge> findAllActiveSignupChallenges(@Param("sessionId") UUID sessionId);
}
