package org.lcr.nvp.global.exception

class BusinessException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)
