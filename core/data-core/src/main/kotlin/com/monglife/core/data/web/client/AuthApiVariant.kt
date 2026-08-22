package com.monglife.core.data.web.client

/**
 * 이 앱이 쓰는 인증 API 계약
 *
 * 앱마다 구글 로그인 방식이 달라 서버로 보낼 수 있는 정보도 다르다.
 * 어느 계약을 쓸지는 app 모듈이 정하고, AuthWebAdapter 가 그에 맞는 엔드포인트를 호출한다.
 */
enum class AuthApiVariant {

    /**
     * 기존 계약 (public/auth/login, public/auth/join)
     *
     * socialAccountId 와 email 만 보낸다. 서버는 이 값을 검증 없이 신뢰한다.
     */
    LEGACY,

    /**
     * Credential 계약 (AuthWebClient.CREDENTIAL_* )
     *
     * 구글이 서명한 idToken 을 함께 보내 서버가 직접 검증할 수 있게 한다.
     * 서버에 해당 엔드포인트가 준비된 뒤에만 쓸 수 있다.
     */
    CREDENTIAL,
}
