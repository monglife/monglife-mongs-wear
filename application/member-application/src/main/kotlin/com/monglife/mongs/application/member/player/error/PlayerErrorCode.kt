package com.monglife.mongs.application.member.player.error

import com.monglife.core.common.error.ErrorCode

enum class PlayerErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_SUBSCRIBE_PLAYER("플레이어 연결 실패", false),
    INVALID_DIS_SUBSCRIBE_PLAYER("플레이어 연결 해제 실패", false),
    INVALID_CREATE_PLAYER("플레이어 등록 실패", false),
    NOT_FOUND_PLAYER("플레이어 조회 실패", false),
    INVALID_BUY_SLOT("슬롯 구매 실패", false),
    INVALID_EXCHANGE_STAR_POINT("스타 포인트 환전 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}