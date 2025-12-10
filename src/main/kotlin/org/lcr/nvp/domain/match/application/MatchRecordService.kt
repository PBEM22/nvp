package org.lcr.nvp.domain.match.application

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFDrawing
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.lcr.nvp.domain.match.domain.MatchRecord
import org.lcr.nvp.domain.match.domain.ScoreRecord
import org.lcr.nvp.domain.match.dto.MatchResultUpdateRequest
import org.lcr.nvp.domain.match.dto.PlayerStatDto
import org.lcr.nvp.domain.match.repository.MatchRecordRepository
import org.lcr.nvp.domain.match.repository.MatchRepository
import org.lcr.nvp.domain.match.repository.ScoreRecordRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class MatchRecordService(
    private val memberRepository: MemberRepository,
    private val matchRepository: MatchRepository,
    private val matchRecordRepository: MatchRecordRepository,
    private val scoreRecordRepository: ScoreRecordRepository,
    private val matchService: MatchService
) {

    @Transactional
    fun importMatchRecords(
        matchId: Long,
        isWin: Boolean,
        teamScore: Int,
        opponentScore: Int,
        mvpMemberId: Long?,
        spikerMemberId: Long?,
        defenderMemberId: Long?,
        inputStream: InputStream
    ) {
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        val stats = parseExcel(inputStream)
        val teamTotalStats = calculateTeamTotals(stats)

        // --- 카운트 업데이트 로직 (Stage 1) ---
        // 업로드된 파일에 있는 선수들 중, 이 경기에 처음 참여하는 선수들을 찾아 카운트를 먼저 업데이트한다.
        val uniquePlayersInUpload = stats.map { it.backNumber to it.playerName }.distinct()
        uniquePlayersInUpload.forEach { (backNumber, playerName) ->
            val member = memberRepository.findByBackNumberAndUser_Name(backNumber, playerName)
                ?: throw NoSuchElementException("선수를 찾을 수 없습니다: 등번호 ${backNumber}, 이름 ${playerName}")

            val isFirstParticipationInMatch = !matchRecordRepository.existsByMemberAndMatch(member, match)

            if (isFirstParticipationInMatch) {
                val scoreRecord = scoreRecordRepository.findByMember(member) ?: ScoreRecord(member = member)
                
                scoreRecord.matchesPlayed += 1

                val isFirstParticipationInTournament = !matchRecordRepository.existsByMemberAndMatch_Tournament(member, match.tournament)
                if (isFirstParticipationInTournament) {
                    scoreRecord.tournamentsPlayed += 1
                }
                scoreRecordRepository.save(scoreRecord)
            }
        }

        // --- 세트별 기록 Upsert 로직 (Stage 2) ---
        stats.forEach { stat ->
            val member = memberRepository.findByBackNumberAndUser_Name(stat.backNumber, stat.playerName)!!
            val scoreRecord = scoreRecordRepository.findByMember(member)!!

            val existingRecord = matchRecordRepository.findByMatchAndMemberAndSetNumber(match, member, stat.setNumber)

            if (existingRecord != null) {
                scoreRecord.subtractMatchRecord(existingRecord)
            }

            val newRecord = MatchRecord(
                id = existingRecord?.id,
                match = match,
                member = member,
                setNumber = stat.setNumber,
                score = stat.score,
                attackAttempt = stat.attackAttempt,
                attackSuccess = stat.attackSuccess,
                attackError = stat.attackError,
                attackBlock = stat.attackBlock,
                receiveAttempt = stat.receiveAttempt,
                receivePerfect = stat.receivePerfect,
                receiveError = stat.receiveError,
                blockAttempt = stat.blockAttempt,
                blockSuccess = stat.blockSuccess,
                blockEffective = stat.blockEffective,
                blockError = stat.blockError,
                blockFault = stat.blockFault,
                serveAttempt = stat.serveAttempt,
                serveAce = stat.serveAce,
                serveError = stat.serveError,
                digAttempt = stat.digAttempt,
                digSuccess = stat.digSuccess,
                digError = stat.digError,
                tossAttempt = stat.tossAttempt,
                tossSuccess = stat.tossSuccess,
                tossError = stat.tossError
            ).apply {
                this.attackSuccessRate = calculateRate(this.attackSuccess, this.attackAttempt)
                this.attackEfficiency = calculateEfficiency(this.attackSuccess - this.attackError - this.attackBlock, this.attackAttempt)
                this.attackPossession = calculateRate(this.attackAttempt, teamTotalStats["attackAttempt"] ?: 1)
                this.receiveSuccessRate = calculateRate(this.receivePerfect, this.receiveAttempt)
                this.receiveEfficiency = calculateEfficiency(this.receivePerfect - this.receiveError, this.receiveAttempt)
                this.blockSuccessRate = calculateRate(this.blockSuccess, this.blockAttempt)
                this.blockEfficiency = calculateEfficiency(this.blockSuccess + this.blockEffective - this.blockError - this.blockFault, this.blockAttempt)
                this.blockPossession = calculateRate(this.blockAttempt, teamTotalStats["blockAttempt"] ?: 1)
                this.serveSuccessRate = calculateRate(this.serveAttempt - this.serveError, this.serveAttempt)
                this.serveEfficiency = calculateEfficiency(this.serveAce - this.serveError, this.serveAttempt)
                this.digSuccessRate = calculateRate(this.digSuccess, this.digAttempt)
                this.digEfficiency = calculateEfficiency(this.digSuccess - this.digError, this.digAttempt)
                this.digPossession = calculateRate(this.digAttempt, teamTotalStats["digAttempt"] ?: 1)
                this.tossSuccessRate = calculateRate(this.tossSuccess, this.tossAttempt)
                this.tossEfficiency = calculateEfficiency(this.tossSuccess - this.tossError, this.tossAttempt)
                this.tossPossession = calculateRate(this.tossAttempt, teamTotalStats["tossAttempt"] ?: 1)
            }

            val savedRecord = matchRecordRepository.save(newRecord)
            
            scoreRecord.addMatchRecord(savedRecord)
            scoreRecordRepository.save(scoreRecord)
        }

        // --- 경기 최종 결과 업데이트 (Stage 3) ---
        val resultRequest = MatchResultUpdateRequest(
            isWin = isWin,
            teamScore = teamScore,
            opponentScore = opponentScore,
            mvpMemberId = mvpMemberId,
            spikerMemberId = spikerMemberId,
            defenderMemberId = defenderMemberId
        )
        matchService.updateMatchResult(matchId, resultRequest)
    }

    fun downloadMatchRecordTemplate(): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("경기 기록 입력")
        val drawing = sheet.createDrawingPatriarch() as XSSFDrawing

        // --- 헤더 구조 정의 ---
        val headerGroups = linkedMapOf(
            "선수 정보" to linkedMapOf(
                "세트번호" to "세트 번호 (1, 2, 3...)",
                "등번호" to "선수의 등번호",
                "선수명" to "선수의 이름"
            ),
            "득점" to linkedMapOf(
                "총득점" to "해당 세트에서 기록한 총 득점"
            ),
            "공격" to linkedMapOf(
                "시도" to "총 공격 시도 횟수", "성공" to "공격 성공(득점) 횟수", "범실" to "공격 범실 횟수", "차단" to "상대 블로킹에 막힌 횟수"
            ),
            "리시브" to linkedMapOf(
                "시도" to "리시브 시도 횟수", "성공" to "정확하게 세터에게 연결된 리시브", "범실" to "리시브 실패"
            ),
            "블로킹" to linkedMapOf(
                "시도" to "블로킹 시도 횟수", "성공" to "블로킹 성공(득점) 횟수", "유효" to "직접 득점은 아니지만 공격 위력을 약화시킨 유효 블로킹", "범실" to "블로킹 네트터치 등 범실 횟수", "실패" to "블로킹 실패 횟수"
            ),
            "서브" to linkedMapOf(
                "시도" to "서브 시도 횟수", "성공" to "서브 에이스(득점) 횟수", "범실" to "서브 범실 횟수"
            ),
            "디그" to linkedMapOf(
                "시도" to "디그 시도 횟수", "성공" to "디그 성공 횟수", "범실" to "디그 범실 횟수"
            ),
            "세트" to linkedMapOf(
                "시도" to "세트(토스) 시도 횟수", "성공" to "세트 성공 횟수", "범실" to "세트 범실 횟수"
            )
        )
        val totalColumnCount = headerGroups.values.sumOf { it.size }

        // --- 스타일 정의 ---
        val titleFont = workbook.createFont().apply { fontHeightInPoints = 16; bold = true }
        val titleStyle = workbook.createCellStyle().apply { setFont(titleFont); alignment = HorizontalAlignment.CENTER; verticalAlignment = VerticalAlignment.CENTER }
        
        val groupHeaderFont = workbook.createFont().apply { bold = true; color = IndexedColors.WHITE.index }
        
        val groupColors = listOf(
            IndexedColors.DARK_BLUE,
            IndexedColors.DARK_GREEN,
            IndexedColors.GREY_50_PERCENT,
            IndexedColors.GREY_50_PERCENT,
            IndexedColors.GREY_50_PERCENT,
            IndexedColors.GREY_50_PERCENT,
            IndexedColors.GREY_50_PERCENT,
            IndexedColors.GREY_50_PERCENT
        )
        
        val groupHeaderStyles = groupColors.map { color ->
            workbook.createCellStyle().apply {
                setFont(groupHeaderFont)
                fillForegroundColor = color.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN; borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
            }
        }
        
        val detailHeaderStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderTop = BorderStyle.THIN; borderBottom = BorderStyle.THIN; borderLeft = BorderStyle.THIN; borderRight = BorderStyle.THIN
        }
        
        val commentTitleStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
        }

        // --- 제목 및 설명 ---
        var currentRowNum = 0
        sheet.addMergedRegion(CellRangeAddress(currentRowNum, currentRowNum, 0, totalColumnCount - 1))
        val titleRow = sheet.createRow(currentRowNum++)
        titleRow.heightInPoints = 30f
        titleRow.createCell(0).apply { setCellValue("NVP 경기 기록 템플릿"); cellStyle = titleStyle }
        currentRowNum++

        // --- 컬럼 설명 주석 추가 ---
        val commentTitleRow = sheet.createRow(currentRowNum++)
        commentTitleRow.createCell(0).apply { setCellValue("--- 컬럼 설명 ---"); cellStyle = commentTitleStyle }
        headerGroups.values.flatMap { it.entries }.forEach { (header, commentText) ->
            sheet.createRow(currentRowNum++).createCell(0).setCellValue("# $header: $commentText")
        }
        currentRowNum++

        // --- 헤더 렌더링 ---
        val groupHeaderRow = sheet.createRow(currentRowNum++)
        val detailHeaderRow = sheet.createRow(currentRowNum)
        var currentCellNum = 0

        headerGroups.entries.forEachIndexed { groupIndex, (groupName, columns) ->
            if (columns.isNotEmpty()) {
                groupHeaderRow.createCell(currentCellNum).apply {
                    setCellValue(groupName)
                    cellStyle = groupHeaderStyles[groupIndex]
                }
                if (columns.size > 1) {
                    sheet.addMergedRegion(CellRangeAddress(groupHeaderRow.rowNum, groupHeaderRow.rowNum, currentCellNum, currentCellNum + columns.size - 1))
                }
            }

            columns.forEach { (header, commentText) ->
                val cell = detailHeaderRow.createCell(currentCellNum)
                cell.setCellValue(header)
                cell.cellStyle = detailHeaderStyle

                val anchor = drawing.createAnchor(0, 0, 0, 0, currentCellNum + 1, detailHeaderRow.rowNum, currentCellNum + 4, detailHeaderRow.rowNum + 4)
                val comment = drawing.createCellComment(anchor)
                comment.string = workbook.creationHelper.createRichTextString(commentText)
                cell.cellComment = comment
                
                currentCellNum++
            }
        }
        
        // --- 최종 컬럼 너비 설정 ---
        // A컬럼 (설명 컬럼)은 내용이 길 수 있으므로 가장 넓게 설정
        sheet.setColumnWidth(0, 50 * 256)
        // B, C 컬럼 (등번호, 선수명)
        sheet.setColumnWidth(1, 12 * 256)
        sheet.setColumnWidth(2, 15 * 256)
        // 나머지 데이터 컬럼들은 숫자 값이므로 통일된 너비로 설정
        (3 until totalColumnCount).forEach { i ->
            sheet.setColumnWidth(i, 12 * 256)
        }
        
        val byteArrayOutputStream = ByteArrayOutputStream()
        workbook.write(byteArrayOutputStream)
        workbook.close()
        return byteArrayOutputStream.toByteArray()
    }

    private fun parseExcel(inputStream: InputStream): List<PlayerStatDto> {
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val playerStats = mutableListOf<PlayerStatDto>()

        val headerRowIndex = findHeaderRow(sheet, "세트번호")
        if (headerRowIndex == -1) throw IllegalArgumentException("유효한 헤더(세트번호)를 찾을 수 없습니다.")
        val dataStartRow = headerRowIndex + 1

        for (i in dataStartRow..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val setNumberCell = row.getCell(0)
            if (setNumberCell == null || setNumberCell.toString().isBlank()) continue

            playerStats.add(
                PlayerStatDto(
                    setNumber = setNumberCell.numericCellValue.toInt(),
                    backNumber = row.getCell(1).numericCellValue.toInt(),
                    playerName = row.getCell(2).stringCellValue,
                    score = row.getCell(3).numericCellValue.toInt(),
                    attackAttempt = row.getCell(4).numericCellValue.toInt(),
                    attackSuccess = row.getCell(5).numericCellValue.toInt(),
                    attackError = row.getCell(6).numericCellValue.toInt(),
                    attackBlock = row.getCell(7).numericCellValue.toInt(),
                    receiveAttempt = row.getCell(8).numericCellValue.toInt(),
                    receivePerfect = row.getCell(9).numericCellValue.toInt(),
                    receiveError = row.getCell(10).numericCellValue.toInt(),
                    blockAttempt = row.getCell(11).numericCellValue.toInt(),
                    blockSuccess = row.getCell(12).numericCellValue.toInt(),
                    blockEffective = row.getCell(13).numericCellValue.toInt(),
                    blockError = row.getCell(14).numericCellValue.toInt(),
                    blockFault = row.getCell(15).numericCellValue.toInt(),
                    serveAttempt = row.getCell(16).numericCellValue.toInt(),
                    serveAce = row.getCell(17).numericCellValue.toInt(),
                    serveError = row.getCell(18).numericCellValue.toInt(),
                    digAttempt = row.getCell(19).numericCellValue.toInt(),
                    digSuccess = row.getCell(20).numericCellValue.toInt(),
                    digError = row.getCell(21).numericCellValue.toInt(),
                    tossAttempt = row.getCell(22).numericCellValue.toInt(),
                    tossSuccess = row.getCell(23).numericCellValue.toInt(),
                    tossError = row.getCell(24).numericCellValue.toInt()
                )
            )
        }
        return playerStats
    }

    private fun findHeaderRow(sheet: org.apache.poi.ss.usermodel.Sheet, headerName: String): Int {
        for (row in sheet) {
            for (cell in row) {
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue.equals(headerName, ignoreCase = true)) {
                    return row.rowNum
                }
            }
        }
        return -1
    }

    private fun calculateTeamTotals(stats: List<PlayerStatDto>): Map<String, Int> {
        return mapOf(
            "attackAttempt" to stats.sumOf { it.attackAttempt },
            "blockAttempt" to stats.sumOf { it.blockAttempt },
            "digAttempt" to stats.sumOf { it.digAttempt },
            "tossAttempt" to stats.sumOf { it.tossAttempt }
        )
    }

    private fun calculateRate(success: Int, attempt: Int): Double {
        if (attempt == 0) return 0.0
        return (success.toDouble() / attempt * 100).roundTo(2)
    }

    private fun calculateEfficiency(numerator: Int, denominator: Int): Double {
        if (denominator == 0) return 0.0
        val efficiency = (numerator.toDouble() / denominator * 100).roundTo(2)
        return max(0.0, efficiency)
    }
    
    private fun Double.roundTo(decimals: Int): Double {
        val multiplier = Math.pow(10.0, decimals.toDouble())
        return (this * multiplier).roundToInt() / multiplier
    }
}

