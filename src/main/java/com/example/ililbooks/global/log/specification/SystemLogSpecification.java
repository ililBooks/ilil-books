package com.example.ililbooks.global.log.specification;

import com.example.ililbooks.global.log.entity.SystemLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

public class SystemLogSpecification {

    public static Specification<SystemLog> withFilters(String traceId, String actionType, String ipAddress) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(traceId)) {
                predicates = cb.and(predicates, cb.equal(root.get("traceId"), traceId));
            }

            if (StringUtils.hasText(actionType)) {
                predicates = cb.and(predicates, cb.equal(root.get("actionType"), actionType));
            }

            if (StringUtils.hasText(ipAddress)) {
                predicates = cb.and(predicates, cb.equal(root.get("ipAddress"), ipAddress));
            }

            return predicates;
        };
    }

    public static Specification<SystemLog> withCreatedAtBetween(Instant start, Instant end) {
        return (root, query, cb) -> cb.between(root.get("createdAt"), start, end);
    }
}