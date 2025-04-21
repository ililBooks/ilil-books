package com.example.ililbooks.domain.user.repository;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByEmailAndLoginType(String email, LoginType loginType);

    Optional<Users> findByEmailAndLoginType(String email, LoginType loginType);

    @Query("SELECT u FROM Users u " +
            "WHERE u.id = :userId " +
            "AND u.isDeleted = false")
    Optional<Users> findById(@Param("userId") Long id);

    @Query("SELECT u FROM Users u " +
            "WHERE u.email = :email " +
            "AND u.isDeleted = false")
    Optional<Users> findByEmail(@Param("email") String email);
}