package com.monglife.core.common.exception

import com.monglife.core.common.error.ErrorCode
import java.util.Collections

open class ErrorException(
    open val code: ErrorCode,
    open val result: Map<String, Any> = Collections.emptyMap(),
    override val message: String = code.getMessage()
) : RuntimeException(message)