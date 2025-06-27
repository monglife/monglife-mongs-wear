package com.monglife.mongs.application.battle.error

import com.monglife.core.common.error.ErrorCode

enum class BattleErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CREATE_QUEUE_PLAYER("매치 큐 등록 실패", false),
    INVALID_DELETE_QUEUE_PLAYER("매치 큐 삭제 실패", false),
    NOT_FOUND_MATCH_REWARD("매치 보상 조회 실패", false),
    NOT_FOUND_WINNER_MATCH_PLAYER("승리 매치 플레이어 조회 실패", false),
    NOT_FOUND_MATCH("매치 조회 실패", false),
    NOT_FOUND_MATCH_PLAYER("매치 플레이어 조회 실패", false),
    INVALID_PUBLISH_MATCH_ENTER("매치 입장 실패", false),
    INVALID_PUBLISH_MATCH_EXIT("매치 퇴장 실패", false),
    INVALID_PUBLISH_MATCH_PICK("매치 선택 실패", false),
    INVALID_SUBSCRIBE_MATCH("매치 구독 실패", false),
    INVALID_DIS_SUBSCRIBE_MATCH("매치 구독 해제 실패", false),
    INVALID_SUBSCRIBE_QUEUE("매치 큐 구독 실패", false),
    INVALID_DIS_SUBSCRIBE_QUEUE("매치 큐 구독 해제 실패", false),
    INVALID_MATCHING("매치 큐 매칭 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}