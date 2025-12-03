package org.lcr.nvp.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // Auth
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "A001", "이미 사용 중인 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A002", "이메일 또는 비밀번호가 일치하지 않습니다."),
    ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "A003", "기본 역할(ROLE_USER)을 찾을 수 없습니다."),

    // Member
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 사용자를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "해당 회원을 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "해당 부서를 찾을 수 없습니다."),
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "해당 직책을 찾을 수 없습니다."),
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "M005", "해당 기수를 찾을 수 없습니다."),
    ASSIGNMENT_DUPLICATION(HttpStatus.BAD_REQUEST, "M006", "이미 해당 기간에 동일한 직책이 할당되어 있습니다."),


    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "J002", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "J003", "유효하지 않은 리프레시 토큰입니다."),
}
