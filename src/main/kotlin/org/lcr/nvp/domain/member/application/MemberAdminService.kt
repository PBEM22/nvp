package org.lcr.nvp.domain.member.application

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.domain.MemberAssignment
import org.lcr.nvp.domain.member.dto.*
import org.lcr.nvp.domain.member.repository.*
import org.lcr.nvp.global.exception.domain.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
@Transactional(readOnly = true)
class MemberAdminService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val roleRepository: RoleRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository,
    private val periodRepository: PeriodRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository,
    private val roleMappingRepository: RoleMappingRepository
) {

    fun getAllMembers(pageable: Pageable): Page<MemberInfoResponse> {
        val memberPage = memberRepository.findAll(pageable)
        val members = memberPage.content

        if (members.isEmpty()) {
            return Page.empty(pageable)
        }

        // 한 번의 쿼리로 모든 회원의 활동 이력을 가져옴 (회원ID, 기간순으로 정렬되어 있음)
        val assignments = memberAssignmentRepository.findAllByMemberInWithDetails(members)
        // 각 회원 ID별로 활동 이력 목록을 그룹화
        val assignmentsByMemberId = assignments.groupBy { it.member.id }

        return memberPage.map { member ->
            // 정렬된 목록에서 첫 번째(가장 최신) 활동 이력을 가져옴
            val latestAssignment = assignmentsByMemberId[member.id]?.firstOrNull()
            MemberInfoResponse(
                memberId = member.id,
                name = member.user.name,
                backNumber = member.backNumber,
                major = member.major,
                membershipStatus = member.membershipStatus,
                periodNumber = latestAssignment?.period?.periodNumber,
                periodYear = latestAssignment?.period?.year,
                departmentName = latestAssignment?.department?.name,
                positionName = latestAssignment?.position?.name,
                displayName = latestAssignment?.displayName
            )
        }
    }

    fun getMemberDetails(memberId: Long): MemberDetailResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberNotFoundException() }

        val assignments = memberAssignmentRepository.findAllByMemberWithDetails(member)

        val assignmentHistoryDtos = assignments.map { assignment ->
            AssignmentHistoryDto(
                departmentName = assignment.department.name,
                positionName = assignment.position.name,
                displayName = assignment.displayName,
                periodYear = assignment.period.year,
                periodSemester = assignment.period.semester,
                periodNumber = assignment.period.periodNumber
            )
        }

        val roles = member.user.roles.map { it.roleName }

        return MemberDetailResponse(
            memberId = member.id,
            userId = member.user.id,
            email = member.user.email,
            name = member.user.name,
            birthday = member.user.birthday,
            isMale = member.user.isMale,
            profileImageUrl = member.profileImageUrl,
            backNumber = member.backNumber,
            major = member.major,
            isPublic = member.isPublic,
            membershipStatus = member.membershipStatus,
            roles = roles,
            assignments = assignmentHistoryDtos
        )
    }

    @Transactional
    fun processMemberExcel(file: MultipartFile): String {
        if (file.isEmpty) {
            throw InvalidInputValueException()
        }

        val dataFormatter = DataFormatter()
        val inputStream: InputStream = file.inputStream
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        var headerRowIndex = -1
        for (i in 0..29) {
            val row = sheet.getRow(i) ?: continue
            val firstCell = row.getCell(0) ?: continue
            if (dataFormatter.formatCellValue(firstCell).equals("email", ignoreCase = true)) {
                headerRowIndex = i
                break
            }
        }
        if (headerRowIndex == -1) {
            throw InvalidInputValueException()
        }

        val headerMap = mutableMapOf<String, Int>()
        val headerRow = sheet.getRow(headerRowIndex)
        headerRow.forEach { cell ->
            headerMap[dataFormatter.formatCellValue(cell).lowercase()] = cell.columnIndex
        }

        val requiredHeaders = listOf("email", "name", "department", "position", "periodnumber")
        requiredHeaders.forEach { header ->
            if (!headerMap.containsKey(header)) {
                throw InvalidInputValueException()
            }
        }

        var successCount = 0
        var failCount = 0
        val errorDetails = mutableListOf<String>()

        for (i in (headerRowIndex + 1)..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val getCellData: (String) -> String = { headerName ->
                val cellIndex = headerMap[headerName.lowercase()] ?: -1
                if (cellIndex == -1) "" else dataFormatter.formatCellValue(row.getCell(cellIndex)).trim()
            }

            val email = getCellData("email")
            if (email.isBlank() || email.startsWith("#")) {
                continue
            }

            try {
                val user = userRepository.findByEmail(email)
                    ?: throw UserNotFoundException()

                // 탈퇴했던 회원인 경우, 계정 부활 처리
                if (user.deletedAt != null) {
                    user.unDelete()
                    user.status = "ACTIVE"
                    memberRepository.findByUser(user)?.apply {
                        this.unDelete()
                        this.membershipStatus = "ACTIVE_MEMBER"
                    }
                }

                // 엑셀 파일의 이름으로 사용자 이름 업데이트
                val nameStr = getCellData("name")
                if (nameStr.isNotBlank() && user.name != nameStr) {
                    user.name = nameStr
                }

                val member = memberRepository.findByUser(user) ?: memberRepository.save(Member(user = user))

                val backNumberStr = getCellData("backnumber")
                if (backNumberStr.isNotBlank()) {
                    member.backNumber = backNumberStr.toIntOrNull() ?: member.backNumber
                }

                val majorStr = getCellData("major")
                if (majorStr.isNotBlank()) {
                    member.major = majorStr
                }

                val departmentName = getCellData("department")
                val positionName = getCellData("position")
                val periodNumberStr = getCellData("periodnumber")

                if (departmentName.isBlank() || positionName.isBlank() || periodNumberStr.isBlank()) {
                    throw IllegalStateException("필수 항목(department, position, periodNumber)이 비어있습니다.")
                }

                val department = departmentRepository.findByName(departmentName)
                    ?: throw DepartmentNotFoundException()
                val position = positionRepository.findByName(positionName)
                    ?: throw PositionNotFoundException()
                val period = periodRepository.findByPeriodNumber(periodNumberStr.toInt())
                    ?: throw PeriodNotFoundException()

                val roleMapping = roleMappingRepository.findByDepartmentAndPosition(department, position)
                    ?: throw DataIntegrityViolationException("부서/직책에 대한 표시 이름 규칙이 정의되지 않았습니다: ${department.name}/${position.name}")

                // 해당 기수(Period)에 대한 할당 정보가 이미 있는지 확인
                val existingAssignment = memberAssignmentRepository.findByMemberAndPeriod(member, period)

                if (existingAssignment != null) {
                    // 이미 해당 기수에 대한 정보가 있으면, 부서와 직책, 표시이름을 덮어쓴다.
                    existingAssignment.department = department
                    existingAssignment.position = position
                    existingAssignment.displayName = roleMapping.displayName
                    memberAssignmentRepository.save(existingAssignment)
                } else {
                    // 해당 기수에 대한 정보가 없으면, 새로 추가한다.
                    memberAssignmentRepository.save(
                        MemberAssignment(
                            member = member,
                            department = department,
                            position = position,
                            period = period,
                            displayName = roleMapping.displayName
                        )
                    )
                }

                memberRepository.save(member)
                successCount++

            } catch (e: Exception) {
                failCount++
                errorDetails.add("Row ${i + 1}: ${e.message}")
            }
        }

        return "엑셀 처리 완료. 총 ${successCount + failCount}건 중 성공: $successCount 건, 실패: $failCount 건. 실패 상세: $errorDetails"
    }

    @Transactional
    fun withdrawMemberById(memberId: Long) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberNotFoundException() }
        val user = member.user

        user.softDelete()
        member.softDelete()
        member.membershipStatus = "WITHDRAWN"
        user.status = "WITHDRAWN" // User의 상태도 변경
    }

    @Transactional
    fun promoteToMember(targetUserId: Long): Member {
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        if (memberRepository.findByUser(user) != null) {
            throw MemberAlreadyExistsException()
        }

        val member = Member(user = user)
        val savedMember = memberRepository.save(member)

        val memberRole = roleRepository.findByRoleName("ROLE_MEMBER")
            ?: throw RoleNotFoundException()
        user.roles.add(memberRole)
        userRepository.save(user)

        return savedMember
    }

    @Transactional
    fun updateMemberStatus(memberId: Long, newStatus: String) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberNotFoundException() }

        member.membershipStatus = newStatus
    }

    @Transactional
    fun updateUserAccountStatus(userId: Long, newStatus: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        // 간단한 유효성 검사. 실제로는 Enum 등으로 관리하는 것이 더 좋음.
        if (newStatus !in listOf("ACTIVE", "SUSPENDED")) {
            throw InvalidInputValueException()
        }
        user.status = newStatus
    }

    @Transactional
    fun updateUserRoles(userId: Long, roleNames: List<String>) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val newRoles = roleNames.map { roleName ->
            roleRepository.findByRoleName(roleName) ?: throw RoleNotFoundException()
        }.toMutableSet()

        user.roles.clear()
        user.roles.addAll(newRoles)
        userRepository.save(user)
    }

    @Transactional
    fun assignPosition(targetUserId: Long, request: AssignPositionRequest): MemberAssignment {
        val user = userRepository.findById(targetUserId)
            .orElseThrow { UserNotFoundException() }

        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()

        val department = departmentRepository.findById(request.departmentId)
            .orElseThrow { DepartmentNotFoundException() }

        val position = positionRepository.findById(request.positionId)
            .orElseThrow { PositionNotFoundException() }

        val period = periodRepository.findById(request.periodId)
            .orElseThrow { PeriodNotFoundException() }

        if (memberAssignmentRepository.existsWithDetails(
                member = member,
                department = department,
                position = position,
                period = period
            )
        ) {
            throw AssignmentDuplicationException()
        }

        val roleMapping = roleMappingRepository.findByDepartmentAndPosition(department, position)
            ?: throw DataIntegrityViolationException("부서/직책에 대한 표시 이름 규칙이 정의되지 않았습니다: ${department.name}/${position.name}")

        val assignment = MemberAssignment(
            member = member,
            department = department,
            position = position,
            period = period,
            displayName = roleMapping.displayName
        )

        return memberAssignmentRepository.save(assignment)
    }

    fun downloadExcelTemplate(): ByteArray {
        // DB에서 최신 부서 및 직책 목록 조회
        val departmentNames = departmentRepository.findAll().joinToString(", ") { it.name }
        val positionNames = positionRepository.findAll().joinToString(", ") { it.name }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Members")

        // 스타일
        val headerFont = workbook.createFont().apply { bold = true }
        val headerCellStyle = workbook.createCellStyle().apply { setFont(headerFont) }
        val commentFont = workbook.createFont().apply { color = org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.index }
        val commentCellStyle = workbook.createCellStyle().apply { setFont(commentFont) }

        // --- 설명 주석 ---
        val instructions = listOf(
            "# [NVP 회원 일괄 등록/수정 템플릿]",
            "# --- 사용법 ---",
            "# 1. 아래 '헤더 행'을 기준으로 데이터를 입력합니다.",
            "# 2. email, name, department, position, periodNumber는 필수 항목입니다.",
            "# 3. '#'으로 시작하는 행은 업로드 시 무시되므로, 이 설명과 예시 데이터는 지우지 않아도 됩니다.",
            "#",
            "# --- 항목 설명 ---",
            "# email: 사용자의 로그인 이메일 (필수)",
            "# name: 사용자 이름 (필수)",
            "# backNumber: 등번호 (선택)",
            "# major: 학과 (선택)",
            "# department: 할당할 부서 이름 (필수, 현재 등록된 부서: ${departmentNames})",
            "# position: 할당할 직책 이름 (필수, 현재 등록된 직책: ${positionNames})",
            "# periodNumber: 할당할 기수 숫자 (필수, 예: 13). 한 학기마다 1씩 올라갑니다.",
            "#",
            "# --- [부서/직책 상세 설명] ---",
            "# 회장단: 파트장(회장), 차장(부회장)",
            "# 훈련부: 파트장(훈련부장), 차장(훈련부)",
            "# 매니저: 파트장(매니저장), 차장(매니저)",
            "# 총무부: 파트장(총무부장), 차장(총무부)",
            "# 관리부: 파트장(관리부장), 차장(타 부서 소속이 아닌 모든 임원진)",
            "# 일반: 일반(일반 부원), 게스트(동아리 소속이 아닌 회원)"
        )
        instructions.forEachIndexed { index, text ->
            sheet.createRow(index).createCell(0).apply {
                setCellValue(text)
                cellStyle = commentCellStyle
            }
        }

        // --- 헤더 행 ---
        val headerRowIndex = instructions.size + 1 // 설명 아래 한 줄 띄고 헤더 시작
        val headers = listOf("email", "name", "backNumber", "major", "department", "position", "periodNumber")
        val headerRow = sheet.createRow(headerRowIndex)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerCellStyle
            }
        }

        // --- 예시 데이터 ---
        val exampleRow = sheet.createRow(headerRowIndex + 1)
        exampleRow.createCell(0).setCellValue("#test@example.com")
        exampleRow.createCell(1).setCellValue("임예시")
        exampleRow.createCell(2).setCellValue("13")
        exampleRow.createCell(3).setCellValue("컴퓨터공학과")
        exampleRow.createCell(4).setCellValue("훈련부")
        exampleRow.createCell(5).setCellValue("일반")
        exampleRow.createCell(6).setCellValue("5")


        // 컬럼 너비 수동 설정
        sheet.setColumnWidth(0, 30 * 256) // email
        sheet.setColumnWidth(1, 15 * 256) // name
        sheet.setColumnWidth(2, 12 * 256) // backNumber
        sheet.setColumnWidth(3, 25 * 256) // major
        sheet.setColumnWidth(4, 15 * 256) // department
        sheet.setColumnWidth(5, 15 * 256) // position
        sheet.setColumnWidth(6, 15 * 256) // periodNumber

        val outputStream = java.io.ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }
}