package com.monglife.core.application.wrapper

data class Page<T>(
    val result: List<T>,
    val page: Int,
    val size: Int,
    val totalPage: Int,
    val isLastPage: Boolean,
)