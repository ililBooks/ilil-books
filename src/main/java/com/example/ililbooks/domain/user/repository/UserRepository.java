package com.example.ililbooks.domain.user.repository;

import com.example.ililbooks.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "WHERE u.id = :userId " +
            "AND u.deletedAt IS NULL")
    Optional<User> findById(@Param("userId") Long id);
}