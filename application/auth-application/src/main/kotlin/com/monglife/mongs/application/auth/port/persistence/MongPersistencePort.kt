package com.monglife.mongs.application.auth.port.persistence

import com.monglife.mongs.domain.mong.model.Mong

interface MongPersistencePort {

    /**
     * 몽 정보 영속화
     */
    suspend fun saveMong(mong: Mong): Mong
}