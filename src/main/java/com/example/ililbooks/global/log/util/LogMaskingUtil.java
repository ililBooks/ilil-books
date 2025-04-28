package com.example.ililbooks.global.log.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogMaskingUtil {

    private final ObjectMapper objectMapper;

    // 무조건 *** 마스킹할 필드
    private static final Set<String> FULL_MASK_FIELDS = Set.of("password", "pwd", "pw", "cardnumber", "ssn");

    // 부분 마스킹할 필드
    private static final Set<String> PARTIAL_MASK_FIELDS = Set.of("email", "contactnumber");

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})(\\d{4})(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(.{2})(.*)(@.*)");

    public Object maskIfNecessary(Object originalBody) {
        if (originalBody == null) {
            return null;
        }

        try {
            JsonNode jsonNode = objectMapper.valueToTree(originalBody);
            if (jsonNode.isObject()) {
                maskFields((ObjectNode) jsonNode);
            }
            return objectMapper.treeToValue(jsonNode, Object.class);
        } catch (Exception e) {
            log.info("LogMasking 실패 - 원본 반환: {}", e.getMessage());
            return originalBody;
        }
    }

    private void maskFields(ObjectNode objectNode) {
        objectNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = objectNode.get(fieldName);
            if (fieldValue == null || fieldValue.isNull()) return;

            String lowerFieldName = fieldName.toLowerCase();

            if (fieldValue.isObject()) {
                maskFields((ObjectNode) fieldValue);
                return;
            }

            if (fieldValue.isTextual()) {
                String value = fieldValue.asText();

                if (FULL_MASK_FIELDS.contains(lowerFieldName)) {
                    objectNode.put(fieldName, "***");
                } else if (PARTIAL_MASK_FIELDS.contains(lowerFieldName)) {
                    if ("email".equals(lowerFieldName)) {
                        objectNode.put(fieldName, maskEmail(value));
                    } else if ("contactnumber".equals(lowerFieldName)) {
                        objectNode.put(fieldName, maskPhoneNumber(value));
                    }
                }
            }
        });
    }

    private String maskPhoneNumber(String phoneNumber) {
        Matcher matcher = PHONE_PATTERN.matcher(phoneNumber);
        if (matcher.matches()) {
            return matcher.group(1) + "-****-" + matcher.group(3);
        }
        log.warn("잘못된 전화번호 형식: {}", phoneNumber);
        return phoneNumber; // 형식이 맞지 않으면 그대로 반환
    }

    private String maskEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (matcher.matches()) {
            return matcher.group(1) + "****" + matcher.group(3);
        }
        log.warn("잘못된 이메일 형식: {}", email);
        return email; // 형식이 맞지 않으면 그대로 반환
    }
}
