package com.monglife.mongs.domain.member.collection.error

import com.monglife.mongs.core.domain.error.ErrorCode

enum class PlayerErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_SUBSCRIBE_PLAYER("플레이어 연결 실패", false),
    INVALID_DIS_SUBSCRIBE_PLAYER("플레이어 연결 해제 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}