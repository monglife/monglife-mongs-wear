package com.monglife.mongs.application.member.player.port.subscribe

import com.monglife.mongs.application.member.player.exception.InvalidDisSubscribePlayerException
import com.monglife.mongs.application.member.player.exception.InvalidSubscribePlayerException
import com.monglife.mongs.application.member.player.port.subscribe.event.PlayerEvent
import kotlinx.coroutines.flow.Flow

interface PlayerSubscribePort {

    /**
     * 플레이어 구독
     */
    @Throws(InvalidSubscribePlayerException::class)
    suspend fun subscribePlayer(accountId: Long): Flow<PlayerEvent>

    /**
     * 플레이어 구독 해제
     */
    @Throws(InvalidDisSubscribePlayerException::class)
    suspend fun disSubscribePlayer(accountId: Long)
}