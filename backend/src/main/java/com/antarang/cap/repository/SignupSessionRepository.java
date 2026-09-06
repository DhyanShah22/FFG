package com.antarang.cap.repository;

import com.antarang.cap.domain.entity.SignupSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SignupSessionRepository extends JpaRepository<SignupSession, UUID> {

    @Query("""
            SELECT s FROM SignupSession s
            JOIN FETCH s.tenant
            WHERE s.id = :id AND s.completedAt IS NULL
            """)
    Optional<SignupSession> findActiveById(@Param("id") UUID id);
}
