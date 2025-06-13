package com.monglife.mongs.core.domain.exception

import com.monglife.mongs.core.domain.error.ErrorCode
import java.util.Collections

open class ErrorException(
    open val code: ErrorCode,
    open val result: Map<String, Any> = Collections.emptyMap(),
    override val message: String = code.getMessage()
) : RuntimeException(message)