package com.example.ililbooks.global.log.repository;

import com.example.ililbooks.global.log.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long>, JpaSpecificationExecutor<SystemLog> {

    Optional<SystemLog> findByTraceId(String traceId);
}
