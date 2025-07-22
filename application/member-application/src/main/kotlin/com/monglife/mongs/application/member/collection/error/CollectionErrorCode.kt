package com.monglife.mongs.application.member.collection.error

import com.monglife.core.common.error.ErrorCode

enum class CollectionErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_SEARCH_COLLECTION_MAP("맵 탐색 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}