package com.monglife.mongs.application.auth.port.web

interface ManagementWebPort {

    /**
     * 몽 목록 조회
     */
    suspend fun getMongs(accountId: Long): List<GetMongResponse>
}