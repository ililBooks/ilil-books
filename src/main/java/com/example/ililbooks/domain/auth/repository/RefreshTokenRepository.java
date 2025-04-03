package com.example.ililbooks.domain.auth.repository;

import com.example.ililbooks.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token ORDER BY rt.id DESC LIMIT 1")
    Optional<RefreshToken> findByToken(@Param("token") String token);
}