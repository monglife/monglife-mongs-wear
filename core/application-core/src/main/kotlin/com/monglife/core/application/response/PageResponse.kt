package com.monglife.core.application.response

data class PageResponse<T>(
    val result: List<T>,
    val page: Int,
    val size: Int,
    val totalPage: Int,
    val isLastPage: Boolean,
)
