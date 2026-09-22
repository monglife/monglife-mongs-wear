package com.monglife.core.data.persistence.entity

import com.monglife.mongs.domain.auth.model.Session

data class SessionEntity(
    val accountId: Long,
    val accessToken: String,
    val refreshToken: String,
    val version: Long,
) {
    fun toDomain() = Session(
        accountId = this.accountId,
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
        version = this.version,
    )
}
