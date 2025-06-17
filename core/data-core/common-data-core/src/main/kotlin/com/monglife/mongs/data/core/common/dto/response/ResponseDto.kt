package com.monglife.mongs.data.core.common.dto.response

data class ResponseDto<T>(

    val code: String?,

    val message: String?,

    val httpStatus: Int?,

    val result: T,
)
