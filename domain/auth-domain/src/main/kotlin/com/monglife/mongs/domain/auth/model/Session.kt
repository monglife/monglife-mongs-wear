package com.monglife.mongs.domain.auth.model

class Session(
    accountId: Long,
    accessToken: String,
    refreshToken: String,
) {
    var accountId: Long = accountId
        private set
    var accessToken: String = accessToken
        private set
    var refreshToken: String = refreshToken
        private set}
