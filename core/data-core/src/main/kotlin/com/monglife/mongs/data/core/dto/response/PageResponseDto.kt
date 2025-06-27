package com.monglife.mongs.data.core.dto.response

data class PageResponseDto<T>(

    val code: String?,

    val message: String?,

    val httpStatus: Int?,

    val result: T,

    val page: Int,

    val size: Int,

    val totalPage: Int,

    val isLastPage: Boolean,
)
