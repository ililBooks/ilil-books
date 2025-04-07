package com.example.ililbooks.domain.user.repository;

import com.example.ililbooks.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u " +
            "WHERE u.id = :userId " +
            "AND u.deletedAt IS NULL")
    Optional<Users> findById(@Param("userId") Long id);
}