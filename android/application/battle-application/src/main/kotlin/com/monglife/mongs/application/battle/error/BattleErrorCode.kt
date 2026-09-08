package com.monglife.mongs.application.battle.error

import com.monglife.core.common.error.ErrorCode

enum class BattleErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CREATE_QUEUE_PLAYER("페이포인트 부족", true),
    INVALID_DELETE_QUEUE_PLAYER("매치 큐 삭제 실패", false),
    NOT_FOUND_MATCH_REWARD("매치 보상 조회 실패", false),
    NOT_FOUND_WINNER_MATCH_PLAYER("승리 매치 플레이어 조회 실패", false),
    INVALID_PUBLISH_MATCH_ENTER("매치 입장 실패", false),
    INVALID_PUBLISH_MATCH_EXIT("매치 퇴장 실패", false),
    INVALID_PUBLISH_MATCH_PICK("매치 선택 실패", true),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}