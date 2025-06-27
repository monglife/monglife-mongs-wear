package com.monglife.mongs.core.domain.wrapper

data class Page<T>(
    val result: List<T>,
    val page: Int,
    val size: Int,
    val totalPage: Int,
    val isLastPage: Boolean,
)