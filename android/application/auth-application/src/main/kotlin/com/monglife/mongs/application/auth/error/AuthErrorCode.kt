package com.monglife.mongs.application.auth.error

import com.monglife.core.common.error.ErrorCode

enum class AuthErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    VERIFY_APP_VERSION("앱 버전 확인 실패", false),
    NEED_JOIN("회원 가입 필요", false),
    INVALID_JOIN("회원 가입 실패", true),
    INVALID_LOGIN("로그인 실패", true),
    INVALID_LOGOUT("로그아웃 실패", true),
    INVALID_CREATE_USER_DEVICE("기기 등록 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}