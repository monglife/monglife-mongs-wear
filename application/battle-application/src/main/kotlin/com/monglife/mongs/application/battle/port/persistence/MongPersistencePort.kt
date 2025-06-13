package com.monglife.mongs.application.battle.port.persistence

import com.monglife.mongs.application.battle.exception.NotFoundMongException
import com.monglife.mongs.domain.mong.model.Mong
import kotlin.jvm.Throws

interface MongPersistencePort {

    /**
     * 선택된 몽 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getCurrentMong(): Mong
}