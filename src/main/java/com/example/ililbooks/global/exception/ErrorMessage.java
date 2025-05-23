package com.example.ililbooks.global.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    NOT_INVALID_USER_ROLE("유효하지 않은 UserRole"),
    NOT_FOUND_TOKEN("토큰을 찾을 수 없습니다."),
    PASSWORD_CONFIRMATION_MISMATCH("비밀번호가 비밀번호 확인과 일치하지 않습니다."),
    DEACTIVATED_USER_EMAIL("탈퇴한 유저 이메일입니다."),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다."),
    EXPIRED_REFRESH_TOKEN("사용이 만료된 refresh token 입니다."),
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다."),
    DUPLICATE_EMAIL("등록된 이메일입니다."),
    USER_EMAIL_NOT_FOUND("가입한 유저의 이메일이 아닙니다."),
    USER_ID_NOT_FOUND("해당 유저의 Id를 찾을 수 없습니다."),
    REFRESH_TOKEN_MUST_BE_STRING("@RefreshToken과 String 타입은 함께 사용되어야 합니다."),
    NOT_NAVER_USER("네이버로 가입된 사용자가 아닙니다."),
    NOT_GOOGLE_USER("구글로 가입된 사용자가 아닙니다."),
    INVALID_USER_INFORMATION("유저 정보가 유효하지 않습니다. 어플리케이션으로 회원가입해주세요."),

    DEFAULT_UNAUTHORIZED("인증이 필요합니다."),
    DEFAULT_BAD_REQUEST("잘못된 요청입니다."),
    DEFAULT_NOT_FOUND("찾지 못했습니다."),
    DEFAULT_FORBIDDEN("권한이 없습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다."),

    INVALID_JWT_SIGNATURE("유효하지 않는 JWT 서명입니다."),
    EXPIRED_JWT_TOKEN("만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN("지원되지 않는 JWT 토큰입니다."),

    NOT_FOUND_BOOK("책을 찾을 수 없습니다."),
    DUPLICATE_BOOK("이미 등록된 책 입니다."),
    CANNOT_UPLOAD_OTHERS_BOOK_IMAGE("다른 사람의 책에 이미지를 업로드 할 수 없습니다."),
    CANNOT_UPDATE_OTHERS_BOOK("다른 사람의 책을 수정할 수 없습니다."),
    BOOK_PARSING_FAILED("도서 정보 파싱 실패"),
    BOOK_API_RESPONSE_FAILED("도서 API 응답 실패"),
    BOOK_ISBN_MISSING("도서의 ISBN이 존재하지 않거나 비어 있습니다."),

    STOCK_UPDATE_CONFLICT("재고 변경 중 충돌이 반복되어 실패했습니다."),

    NOT_FOUND_BOOK_DOCUMENT("Book document 를 찾을 수 없습니다."),
    BLANK_KEYWORD_NOT_ALLOWED("검색어를 입력해주세요."),

    DUPLICATE_REVIEW("하나의 책에 하나의 리뷰만 등록할 수 있습니다."),
    NOT_FOUND_REVIEW("리뷰를 찾을 수 없습니다."),
    CANNOT_UPDATE_OTHERS_REVIEW("다른 사람의 리뷰를 수정할 수 없습니다."),
    CANNOT_DELETE_OTHERS_REVIEW("다른 사람의 리뷰를 삭제할 수 없습니다."),
    CANNOT_UPDATE_OTHERS_REVIEW_IMAGE("다른 사람의 리뷰에 이미지를 업로드할 수 없습니다."),

    EVENT_REGISTRATION_ONLY_FOR_LIMITED_EDITION("한정판 책만 이벤트에 등록할 수 있습니다."),
    ALREADY_STARTED_EVENT_DELETE_NOT_ALLOWED("이미 시작된 행사는 삭제할 수 없습니다."),
    ALREADY_RESERVED_EVENT("이미 예약된 행사입니다."),
    NOT_FOUND_USER("유저 확인 불가"),
    NOT_FOUND_EVENT("행사 확인 불가"),
    NOT_OWN_RESERVATION("본인의 예약이 아닙니다."),
    NOT_FOUND_RESERVATION("예약 확인 불가"),
    CANNOT_DELETE_OTHERS_IMAGE("다른 사람의 이미지를 삭제할 수 없습니다."),
    NOT_FOUND_IMAGE("이미지가 존재하지 않습니다."),
    FAILED_UPLOAD_IMAGE("이미지 업로드에 실패하였습니다."),
    UNSUPPORTED_IMAGE_PROCESSING_TYPE("지원하지 않는 이미지 처리 타입입니다: "),
    IMAGE_ALREADY_EXISTS("해당 책에는 이미 업로드된 이미지가 있습니다."),
    FAILED_DELETE_IMAGE("이미지 삭제에 실패하였습니다."),
    IMAGE_UPLOAD_LIMIT_OVER("등록 가능한 이미지 개수를 초과하였습니다."),
    NO_PERMISSION("권한이 없습니다."),
    RESERVATION_NOT_SUCCESS("예약이 성공하지 못했습니다."),
    RESERVATION_EXPIRED("예약시간이 만료되었습니다."),
    ALREADY_ORDERED("이미 주문하신 상품입니다."),
    DUPLICATE_POSITION_INDEX("이미 해당 위치에 이미지가 존재합니다."),

    NOT_EXIST_SHOPPING_CART("장바구니가 존재하지 않습니다."),
    CANNOT_ADD_BOOK_TO_CART("장바구니에 담을 수 없는 책입니다."),
    CART_QUANTITY_INVALID("장바구니 수량은 0 이상이어야 합니다."),
    OUT_OF_STOCK("요청하신 수량보다 재고가 부족합니다."),

    NOT_FOUND_ORDER("주문 확인 불가"),
    NOT_OWN_ORDER("본인 외 주문 확인 불가"),
    CANNOT_CANCEL_ORDER("해당 주문은 취소할 수 없습니다."),
    CANNOT_CHANGE_ORDER("주문 대기 상태만 주문 승인 할 수 있습니다."),
    CANNOT_START_DELIVERY("주문 승인 상태만 배송할 수 있습니다."),
    COMPLETE_DELIVERY("배송 완료 상태입니다."),
    CANNOT_DELIVERY_CANCELLED_ORDER("취소된 주문은 배송이 불가합니다."),
    CANNOT_DELIVERY_ORDER("결제 승인 상태가 아니라 배송이 불가합니다."),

    NOT_FOUND_PAYMENT("결제를 찾을 수 없습니다."),
    CANNOT_CREATE_PAYMENT("주문 대기 상태가 아니라 결제를 생성할 수 없습니다."),
    NOT_OWN_PAYMENT("본인 외 결제 접근 불가"),
    CANNOT_REQUEST_PAYMENT("결제 준비 상태가 아니라 결제를 요청할 수 없습니다."),
    CANNOT_CANCEL_PAYMENT("결제 상태가 아니라 결제를 취소할 수 없습니다."),
    CANCEL_PAYMENT_FAILED("결제 취소를 실패했습니다."),
    INVALID_RESERVATION_STATUS_FOR_PAYMENT("예약이 결제 대기 상태가 아닐 경우 결제를 할 수 없습니다."),

    NAVER_API_RESPONSE_FAILED("네이버 api 응답 실패"),
    NAVER_PARSING_FAILED("네이버 파싱 실패"),
    NOT_FOUND_PROFILE("프로필을 찾을 수 없습니다."),
    INVALID_STATE("CSRF 방지를 위한 state 값이 일치하지 않습니다."),

    GOOGLE_API_RESPONSE_FAILED("구글 API 응답 실패"),
    GOOGLE_PARSING_FAILED("구글 파싱 실패"),

    REDIS_PARSING_FAILED("Redis 정보 파싱 실패"),
    REDIS_SERIALIZE_FAILED("Redis 정보 직렬화 실패"),

    FAILED_SEND_MAIL("메일 발송에 실패"),
    BOOK_ID_NOT_FOUND_IN_REDIS("해당 키에 해당하는 BOOK ID 존재하지 않음"),
    PERIOD_TYPE_NOT_FOUND("PeriodType 조회 실패"),

    LOG_NOT_FOUND("로그를 찾을 수 없습니다."),

    REDISSON_LOCK_FAILED("락 획득에 실패했습니다."),
    REDISSON_LOCK_INTERRUPTED("Redisson 락 처리 중 인터럽트 발생");


    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }
}
