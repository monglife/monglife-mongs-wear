package com.monglife.mongs.core.data.dto.response

data class ResponseDto<T>(

    val code: String?,

    val message: String?,

    val httpStatus: Int?,

    val result: T,
)
