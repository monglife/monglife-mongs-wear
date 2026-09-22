package com.monglife.core.auth.error

import com.monglife.core.common.error.ErrorCode

enum class AuthCoreErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    // 취소는 오류가 아니다. 버튼이 그대로 남아 재시도가 자명하므로 토스트를 띄우지 않는다.
    GOOGLE_LOGIN_CANCELED("구글 로그인 취소", false),
    // Credential Manager(mobile) 경로 전용. legacy 는 계정이 없어도 GMS 가 계정 추가 흐름을
    // 띄우고, 사용자가 취소하면 SIGN_IN_CANCELLED(12501) 로 돌아와 이 코드에 도달하지 않는다.
    NO_GOOGLE_ACCOUNT("기기에 Google 계정을 추가해 주세요", true),
    GOOGLE_LOGIN_FAILED("구글 로그인 실패", true),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}
