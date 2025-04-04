package com.example.ililbooks.global.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationMessage {

    public static final String NOT_BLANK_EMAIL = "이메일은 필수 입력 값입니다.";
    public static final String PATTERN_EMAIL = "이메일 형식으로 입력되어야 합니다.";
    public static final String NOT_BLANK_NICKNAME = "닉네임은 필수 입력 값입니다.";
    public static final String NOT_BLANK_PASSWORD = "비밀번호는 필수 입력 값입니다.";
    public static final String NOT_NULL_SCORE = "별점은 필수 입력 값입니다.";
    public static final String NOT_BLANK_CONTENT = "컨텐트는 필수 입력 값입니다.";
    public static final String NOT_BLANK_PRODUCT_NAME = "상품명은 필수 입력 값입니다.";
    public static final String NOT_NULL_CATEGORY = "카테고리는 필수 입력 값입니다.";
    public static final String NOT_NULL_PRICE = "가격은 필수 입력 값입니다.";
    public static final String PATTERN_PASSWORD = "비밀번호는 영어, 숫자 포함 8자리 이상이어야 합니다.";
    public static final String PATTERN_PASSWORD_REGEXP = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
    public static final String NOT_BLANK_EVENT_NAME = "이벤트 이름은 필수 입력 값입니다.";
    public static final String NOT_BLANK_EVENT_DESCRIPTION = "이벤트 설명은 필수 입력 값입니다.";
    public static final String INVALID_EVENT_QUANTITY = "수량은 1개 이상이어야 합니다.";
    public static final String NOT_NULL_START_DATE = "시작 날짜는 필수 입력 값입니다.";
    public static final String NOT_NULL_END_DATE = "종료 날짜는 필수 입력 값입니다.";
    public static final String NOT_NULL_AUTHOR = "저자는 필수 입력 값입니다.";
    public static final String NOT_NULL_TITLE = "책 제목은 필수 입력 값입니다.";
    public static final String INVALID_STOCK = "재고는 0 이상이어야 합니다.";
    public static final String NOT_NULL_SALE_STATUS = "판매 상태를 입력해야합니다.";
    public static final String NOT_NULL_LIMITED_TYPE = "한정판 상태를 입력해야합니다.";
    public static final String NOT_NULL_BOOK_ID = "도서 ID는 필수 입력 값입니다.";
    public static final String NOT_NULL_EVENT_TITLE = "행사 제목은 필수 입력 값입니다.";
    public static final String FUTURE_EVENT_END_DATE = "행사 종료일은 현재 시각보다 이후여야 합니다.";

}