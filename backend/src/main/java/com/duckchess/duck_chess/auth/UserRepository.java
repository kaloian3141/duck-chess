package com.duckchess.duck_chess.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    public Optional<UserEntity> findByEmail(String email);

    public Optional<UserEntity> findByUsername(String username);

    public boolean existsByEmail(String email);

    public boolean existsByUsername(String username);
    

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM UserEntity u
        WHERE u.emailVerified = false
          AND u.createdAt < :cutoff
        """)
    int deleteUnverifiedOlderThan(@Param("cutoff") OffsetDateTime cutoff);
}
