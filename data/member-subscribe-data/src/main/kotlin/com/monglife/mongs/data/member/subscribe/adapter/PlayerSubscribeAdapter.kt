package com.monglife.mongs.data.member.subscribe.adapter

import com.monglife.mongs.application.member.player.port.subscribe.PlayerSubscribePort
import javax.inject.Inject

class PlayerSubscribeAdapter @Inject constructor(

) : PlayerSubscribePort {

    override suspend fun subscribePlayer(accountId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun disSubscribePlayer(accountId: Long) {
        TODO("Not yet implemented")
    }
}