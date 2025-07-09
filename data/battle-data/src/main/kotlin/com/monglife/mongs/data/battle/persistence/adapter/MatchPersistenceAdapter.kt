package com.monglife.mongs.data.battle.persistence.adapter

import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.data.battle.persistence.db.BattleRoomDB
import com.monglife.mongs.data.battle.persistence.entity.MatchEntity
import com.monglife.mongs.data.battle.persistence.entity.MatchPlayerEntity
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MatchPersistenceAdapter @Inject constructor(
    private val roomDB: BattleRoomDB,
) : MatchPersistencePort {

    /**
     * 매치 객체 조회
     */
    @Throws(NotFoundMatchException::class)
    override suspend fun getMatch(matchId: Long): Match =
        roomDB.matchDao().findMatchByMatchId(matchId = matchId)?.toDomain()
            ?: throw NotFoundMatchException()

    /**
     * 최근 매치 객체 조회
     */
    @Throws(NotFoundMatchException::class)
    override suspend fun getLeastMatch(): Match =
        roomDB.matchDao().findTopMatch()?.toDomain()
            ?: throw NotFoundMatchException()

    /**
     * 매치 라이브 객체 조회
     */
    override suspend fun getMatchFlow(matchId: Long): Flow<Match?> =
        roomDB.matchDao().findMatchLiveByMatchId(matchId = matchId)
            .map { matchEntity -> matchEntity?.toDomain() }

    /**
     * 매치 플레이어 객체 조회
     */
    @Throws(NotFoundMatchPlayerException::class)
    override suspend fun getMatchPlayer(playerId: String): MatchPlayer =
        roomDB.matchPlayerDao().findMatchPlayerByPlayerId(playerId = playerId)?.toDomain()
            ?: throw NotFoundMatchException()

    /**
     * 매치 플레이어 객체 목록 조회
     */
    override suspend fun getMatchPlayers(matchId: Long): List<MatchPlayer> =
        roomDB.matchPlayerDao().findAllMatchPlayerByPlayerId(matchId = matchId).map {
            it.toDomain()
        }

    /**
     * 매치 플레이어 라이브 객체 목록 조회
     */
    override suspend fun getMatchPlayersFlow(matchId: Long): Flow<List<MatchPlayer>> =
        roomDB.matchPlayerDao().findAllMatchPlayerFlowByMatchId(matchId = matchId).map {
            it.map { matchPlayerEntity -> matchPlayerEntity.toDomain() }
        }

    /**
     * 매치 수정
     */
    override suspend fun saveMatch(match: Match): Match =
        roomDB.matchDao().save(
            matchEntity = MatchEntity(
                matchId = match.matchId,
                round = match.round,
                isLastRound = match.isLastRound,
                stateCode = match.stateCode,
                createdAt = match.createdAt,
            )
        ).toDomain()

    /**
     * 매치 플레이어 수정
     */
    override suspend fun saveMatchPlayer(matchPlayer: MatchPlayer): MatchPlayer =
        roomDB.matchPlayerDao().save(
            matchPlayerEntity = MatchPlayerEntity(
                playerId = matchPlayer.playerId,
                matchId = matchPlayer.matchId,
                mongCode = matchPlayer.mongCode,
                mongName = matchPlayer.mongName,
                name = matchPlayer.name,
                hp = matchPlayer.hp,
                roundCode = matchPlayer.roundCode,
            )
        ).toDomain()

    /**
     * 매치 삭제
     */
    override suspend fun deleteAllMatch() {
        roomDB.matchDao().deleteAll()
        roomDB.matchPlayerDao().deleteAll()
    }
}