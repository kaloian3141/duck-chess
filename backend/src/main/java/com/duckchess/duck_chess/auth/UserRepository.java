package com.duckchess.duck_chess.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    public Optional<UserEntity> findByEmail(String email);

    public Optional<UserEntity> findByUsername(String username);

    public boolean existsByEmail(String email);

    public boolean existsByUsername(String username);
    
}
