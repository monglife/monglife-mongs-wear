package com.monglife.mongs.data.core.web.dto.response

data class ResponseDto<T>(

    val code: String?,

    val message: String?,

    val httpStatus: Int?,

    val result: T,
)
