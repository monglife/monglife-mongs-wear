package com.monglife.mongs.application.mong.error

import com.monglife.core.common.error.ErrorCode

enum class MongErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    NOT_FOUND_MONG("몽 조회 실패", false),
    NOT_FOUND_TRAINING("훈련 조회 실패", false),
    INVALID_TRAINING("훈련 완료 실패", true),
    INVALID_FEED_FOOD("음식 섭취 실패", true),
    INVALID_FEED_SNACK("간식 섭취 실패", true),
    INVALID_CONSUME_INVENTORY("인벤 소비 실패", false),
    INVALID_BUY_RANDOM_DRAW_TICKET("랜덤 뽑기 티켓 구매 실패", false),
    INVALID_RANDOM_DRAW("랜덤 뽑기 실패", true),
    INVALID_CREATE_MONG("몽 생성 실패", true),
    INVALID_DELETE_MONG("몽 삭제 실패", true),
    INVALID_STROKE_MONG("쓰다듬기 실패", true),
    INVALID_SLEEPING_MONG("잠자기 실패", true),
    INVALID_POOP_CLEAN_MONG("청소 실패", true),
    INVALID_EVOLUTION_MONG("진화 실패", true),
    INVALID_GRADUATE_MONG("졸업 실패", true),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}