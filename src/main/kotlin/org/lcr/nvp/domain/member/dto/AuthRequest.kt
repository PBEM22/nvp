package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "회원가입 요청 DTO")
data class SignupRequest(
    @Schema(description = "이메일 주소", example = "test@example.com")
    val email: String,
    @Schema(description = "비밀번호 (8~16자 영문, 숫자 조합)", example = "password123")
    val password: String,
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,
    @Schema(description = "생년월일", example = "2000-01-15")
    val birthday: LocalDate,
    @Schema(description = "성별 ('남성' 또는 '여성')", example = "남성")
    val gender: String
)

@Schema(description = "로그인 요청 DTO")
data class LoginRequest(
    @Schema(description = "이메일 주소", example = "test@example.com")
    val email: String,
    @Schema(description = "비밀번호", example = "password123")
    val password: String
)
