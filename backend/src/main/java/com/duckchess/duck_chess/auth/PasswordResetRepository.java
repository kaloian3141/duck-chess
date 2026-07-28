package com.duckchess.duck_chess.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetEntity, Long> 
{

    Optional<PasswordResetEntity>
        findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM PasswordResetEntity r
        WHERE r.usedAt IS NOT NULL
           OR r.expiresAt < :cutoff
        """)
    int deleteUsedOrExpired(@Param("cutoff") OffsetDateTime cutoff);
}