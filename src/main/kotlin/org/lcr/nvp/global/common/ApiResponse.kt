package org.lcr.nvp.global.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 변환 시 제외
data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T? = null
) {
    companion object {
        // 성공 응답
        fun <T> onSuccess(result: T): ApiResponse<T> {
            return ApiResponse(
                isSuccess = true,
                code = "SUCCESS",
                message = "요청에 성공하였습니다.",
                result = result
            )
        }

        // 데이터 없는 성공 응답
        fun onSuccess(): ApiResponse<Unit> {
            return ApiResponse(
                isSuccess = true,
                code = "SUCCESS",
                message = "요청에 성공하였습니다."
            )
        }

        // 실패 응답
        fun onFailure(code: String, message: String): ApiResponse<Unit> {
            return ApiResponse(
                isSuccess = false,
                code = code,
                message = message
            )
        }
    }
}
