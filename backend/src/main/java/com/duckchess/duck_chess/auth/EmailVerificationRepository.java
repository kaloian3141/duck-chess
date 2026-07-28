package com.duckchess.duck_chess.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long>
{
    Optional<EmailVerificationEntity> findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(Long userId);
    
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM EmailVerificationEntity v
        WHERE v.verifiedAt IS NOT NULL
           OR v.expiresAt < :cutoff
        """)
    int deleteVerifiedOrExpired(@Param("cutoff") OffsetDateTime cutoff);
}
