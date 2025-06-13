package com.monglife.mongs.domain.member.player.repository

import kotlinx.coroutines.flow.Flow

interface PlayerRepository {

    /**
     * 플레이어 정보 갱신
     */
    suspend fun updatePlayer()

    /**
     * 플레이어 정보 등록
     */
    suspend fun createPlayer()

    /**
     * 플레이어 스타 포인트 라이브 객체 조회
     */
    suspend fun getStarPointLive(): Flow<Int>

    /**
     * 플레이어 슬롯 갯수 조회
     */
    suspend fun getSlotCount(): Int

    /**
     * 플레이어 슬롯 구매
     */
    suspend fun buySlot()

    /**
     * 플레이어 스타 포인트 환전
     */
    suspend fun exchangeStarPoint(mongId: Long, starPoint: Int)
}