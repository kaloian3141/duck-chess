package com.duckchess.duck_chess.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long>
{
    Optional<EmailVerificationEntity> findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(Long userId);    
}
