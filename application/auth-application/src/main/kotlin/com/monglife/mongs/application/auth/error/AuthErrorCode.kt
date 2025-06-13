package com.monglife.mongs.application.auth.error

import com.monglife.mongs.core.domain.error.ErrorCode

enum class AuthErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    VERIFY_APP_VERSION("앱 버전 검증 실패", false),
    NEED_UPDATE_APP("앱 업데이트 필요", true),
    NEED_JOIN("회원 가입 필요", false),
    JOIN("회원 가입 실패", false),
    LOGIN("로그인 실패", true),
    LOGOUT("로그 아웃 실패", true),
    NOT_FOUND_PLAYER("플레이어 조회 실패", false),
    INVALID_CREATE_PLAYER("플레이어 등록 실패", false),
    INVALID_CREATE_USER_DEVICE("기기 등록 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}