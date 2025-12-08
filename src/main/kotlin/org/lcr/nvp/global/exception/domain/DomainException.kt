package org.lcr.nvp.global.exception.domain

import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode

// User / Member
class UserNotFoundException : BusinessException(ErrorCode.USER_NOT_FOUND)
class MemberNotFoundException : BusinessException(ErrorCode.MEMBER_NOT_FOUND)
class EmailDuplicationException : BusinessException(ErrorCode.EMAIL_DUPLICATION)
class MemberAlreadyExistsException : BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS)

// Auth
class RoleNotFoundException : BusinessException(ErrorCode.ROLE_NOT_FOUND)
class LoginFailedException : BusinessException(ErrorCode.LOGIN_FAILED)
class InvalidRefreshTokenException : BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)

// Common
class InvalidInputValueException : BusinessException(ErrorCode.INVALID_INPUT_VALUE)
class DataIntegrityViolationException(message: String) : BusinessException(ErrorCode.DATA_INTEGRITY_VIOLATION, message)

// Period / Assignment
class PeriodNotFoundException : BusinessException(ErrorCode.PERIOD_NOT_FOUND)
class PeriodNumberDuplicationException : BusinessException(ErrorCode.PERIOD_NUMBER_DUPLICATION)
class DepartmentNotFoundException : BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND)
class PositionNotFoundException : BusinessException(ErrorCode.POSITION_NOT_FOUND)
class AssignmentDuplicationException : BusinessException(ErrorCode.ASSIGNMENT_DUPLICATION)

// Attendance
class InvalidAttendanceCodeException : BusinessException(ErrorCode.INVALID_ATTENDANCE_CODE)
class AttendanceCodeAlreadyExistsException : BusinessException(ErrorCode.ATTENDANCE_CODE_ALREADY_EXISTS)
class ExerciseDateNotFoundException : BusinessException(ErrorCode.EXERCISE_DATE_NOT_FOUND)
