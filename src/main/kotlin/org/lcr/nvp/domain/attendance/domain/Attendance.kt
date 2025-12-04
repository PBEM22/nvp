package org.lcr.nvp.domain.attendance.domain

import jakarta.persistence.*
import org.lcr.nvp.domain.member.domain.Member

@Entity
@Table(name = "attendances")
class Attendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_date_id", nullable = false)
    val exerciseDate: ExerciseDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "round1_status", nullable = false)
    var round1Status: AttendanceStatus = AttendanceStatus.ABSENT,

    @Enumerated(EnumType.STRING)
    @Column(name = "round2_status", nullable = false)
    var round2Status: AttendanceStatus = AttendanceStatus.ABSENT

) {
    /**
     * 1, 2회차 출석 상태를 조합하여 최종 출석 상태를 계산합니다.
     */
    fun getFinalStatus(): String {
        return when {
            round1Status == AttendanceStatus.PRESENT && round2Status == AttendanceStatus.PRESENT -> "출석"
            round1Status == AttendanceStatus.ABSENT && round2Status == AttendanceStatus.PRESENT -> "지각"
            round1Status == AttendanceStatus.PRESENT && round2Status == AttendanceStatus.ABSENT -> "조퇴"
            else -> "결석"
        }
    }
}
