package com.monglife.mongs.data.member.player.subscribe.adapter

import com.monglife.mongs.application.member.player.exception.InvalidDisSubscribePlayerException
import com.monglife.mongs.application.member.player.exception.InvalidSubscribePlayerException
import com.monglife.mongs.application.member.player.port.subscribe.PlayerSubscribePort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerSubscribeAdapter @Inject constructor(

) : PlayerSubscribePort {

    /**
     * 플레이어 구독
     */
    @Throws(InvalidSubscribePlayerException::class)
    override suspend fun subscribePlayer(accountId: Long) {
        TODO("Not yet implemented")
    }

    /**
     * 플레이어 구독 해제
     */
    @Throws(InvalidDisSubscribePlayerException::class)
    override suspend fun disSubscribePlayer(accountId: Long) {
        TODO("Not yet implemented")
    }
}