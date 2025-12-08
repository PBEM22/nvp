package org.lcr.nvp.global.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String
) : RuntimeException(message) {
    constructor(errorCode: ErrorCode) : this(errorCode, errorCode.message)
}
