package com.monglife.mongs.domain.auth.model

class Session(
    accountId: Long,
    accessToken: String,
    refreshToken: String,
    version: Long,
) {
    var accountId: Long = accountId
        private set
    var accessToken: String = accessToken
        private set
    var refreshToken: String = refreshToken
        private set
    var version: Long = version
        private set

    /**
     * 토큰 동기화
     */
    fun update(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    /**
     * 세션 버전 증가
     */
    fun increaseVersion() {
        this.version += 1
    }
}
