package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.lcr.nvp.domain.match.application.MatchRecordService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Tag(name = "경기 기록 관리 API", description = "경기 기록 템플릿 다운로드 및 엑셀 파일 업로드 API")
@RestController
@RequestMapping("/api/v1/matches")
class MatchRecordController(
    private val matchRecordService: MatchRecordService
) {

    @Operation(summary = "경기 기록 XLSX 템플릿 다운로드", description = "선수별 경기 기록을 입력하는 데 사용되는 XLSX 템플릿 파일을 다운로드합니다.")
    @GetMapping("/records/template")
    fun downloadMatchRecordTemplate(): ResponseEntity<ByteArray> {
        val excelBytes = matchRecordService.downloadMatchRecordTemplate()
        val fileName = URLEncoder.encode("NVP_경기기록_템플릿.xlsx", StandardCharsets.UTF_8)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$fileName")
            .body(excelBytes)
    }

    @Operation(summary = "경기 기록 XLSX 파일 업로드 및 결과 입력", description = "작성된 경기 기록 XLSX 파일을 업로드하고, 경기의 최종 결과를 함께 저장합니다.")
    @PostMapping("/{matchId}/records/upload", consumes = ["multipart/form-data"])
    fun uploadMatchRecords(
        @Parameter(description = "기록을 추가할 경기의 ID") @PathVariable matchId: Long,
        @Parameter(description = "경기 승리 여부") @RequestParam("isWin") isWin: Boolean,
        @Parameter(description = "우리 팀의 최종 세트 스코어") @RequestParam("teamScore") teamScore: Int,
        @Parameter(description = "상대 팀의 최종 세트 스코어") @RequestParam("opponentScore") opponentScore: Int,
        @Parameter(description = "업로드할 경기 기록 XLSX 파일") @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Void> {
        matchRecordService.importMatchRecords(matchId, isWin, teamScore, opponentScore, file.inputStream)
        return ResponseEntity.ok().build()
    }
}
