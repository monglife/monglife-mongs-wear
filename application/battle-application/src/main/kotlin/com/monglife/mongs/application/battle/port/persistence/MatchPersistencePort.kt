package com.monglife.mongs.application.battle.port.persistence

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.flow.Flow

interface MatchPersistencePort {

    /**
     * 매치 객체 조회
     */
    @Throws(NotFoundMatchException::class)
    suspend fun getMatch(matchId: Long): Match

    /**
     * 최근 매치 객체 조회
     */
    @Throws(NotFoundMatchException::class)
    suspend fun getLeastMatch(): Match

    /**
     * 매치 라이브 객체 조회
     */
    suspend fun getMatchFlow(matchId: Long): Flow<Match?>

    /**
     * 매치 플레이어 조회
     */
    @Throws(NotFoundMatchPlayerException::class)
    suspend fun getMatchPlayer(playerId: String): MatchPlayer

    /**
     * 매치 플레이어 객체 목록 조회
     */
    suspend fun getMatchPlayers(matchId: Long): List<MatchPlayer>

    /**
     * 매치 플레이어 라이브 객체 목록 조회
     */
    suspend fun getMatchPlayersFlow(matchId: Long): Flow<List<MatchPlayer>>

    /**
     * 매치 수정
     */
    suspend fun saveMatch(match: Match): Match

    /**
     * 매치 플레이어 수정
     */
    suspend fun saveMatchPlayer(matchPlayer: MatchPlayer): MatchPlayer

    /**
     * 모든 매치 삭제
     */
    suspend fun deleteAllMatch()
}