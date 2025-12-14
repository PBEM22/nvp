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
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "해당 요청에 대한 권한이 없습니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "C004", "데이터 무결성 위반, 요청을 처리할 수 없습니다."),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청하신 URL을 찾을 수 없습니다."),

    // Auth
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "A001", "이미 사용 중인 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A002", "이메일 또는 비밀번호가 일치하지 않습니다."),
    ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "A003", "기본 역할(ROLE_USER)을 찾을 수 없습니다."),
    ACCOUNT_DEACTIVATED(HttpStatus.FORBIDDEN, "A004", "탈퇴하여 비활성화된 계정입니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "A005", "활동 정지된 계정입니다."),

    // Member
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 사용자를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "해당 회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "M003", "이미 정식 회원으로 등록된 사용자입니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "해당 부서를 찾을 수 없습니다."),
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "M005", "해당 직책을 찾을 수 없습니다."),
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "M006", "해당 기수를 찾을 수 없습니다."),
    ASSIGNMENT_DUPLICATION(HttpStatus.BAD_REQUEST, "M007", "이미 해당 기간에 동일한 직책이 할당되어 있습니다."),
    PERIOD_NUMBER_DUPLICATION(HttpStatus.BAD_REQUEST, "M008", "이미 존재하는 기수 번호입니다."),
    CURRENT_PERIOD_NOT_SET(HttpStatus.NOT_FOUND, "M009", "현재 활동 기수가 설정되어 있지 않습니다."),

    // Board
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "해당 게시글을 찾을 수 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM01", "해당 댓글을 찾을 수 없습니다."),

    // Match, Tournament, etc.
    TOURNAMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MT01", "해당 대회를 찾을 수 없습니다."),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MT02", "해당 경기를 찾을 수 없습니다."),

    // Attendance
    ATTENDANCE_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "AT01", "이미 유효한 출석 코드가 존재합니다."),
    INVALID_ATTENDANCE_CODE(HttpStatus.BAD_REQUEST, "AT02", "출석 코드가 유효하지 않습니다."),
    EXERCISE_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "AT03", "해당 운동일을 찾을 수 없습니다."),

    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "J002", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "J003", "유효하지 않은 리프레시 토큰입니다."),
}
