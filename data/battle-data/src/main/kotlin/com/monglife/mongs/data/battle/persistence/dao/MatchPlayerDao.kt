package com.monglife.mongs.data.battle.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.monglife.mongs.data.battle.persistence.entity.MatchPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchPlayerDao {

    /**
     * 매치 플레이어 조회
     */
    @Query("SELECT * FROM match_player WHERE playerId = :playerId")
    fun findMatchPlayerByPlayerId(playerId: String): MatchPlayerEntity?

    /**
     * 매치 플레이어 목록 조회
     */
    @Query("SELECT * FROM match_player WHERE matchId = :matchId")
    fun findAllMatchPlayerByPlayerId(matchId: Long): List<MatchPlayerEntity>

    /**
     * 매치 플레이어 라이브 엔티티 목록 조회
     */
    @Query("SELECT * FROM match_player WHERE matchId = :matchId")
    fun findAllMatchPlayerFlowByMatchId(matchId: Long): Flow<List<MatchPlayerEntity>>

    /**
     * 모든 매치 플레이어 삭제
     */
    @Query("DELETE FROM match_player")
    fun deleteAll()

    /**
     * 매치 플레이어 등록
     */
    @Insert(entity = MatchPlayerEntity::class)
    fun insert(matchPlayerEntity: MatchPlayerEntity): Long

    /**
     * 매치 플레이어 수정
     */
    @Update(entity = MatchPlayerEntity::class)
    fun update(matchPlayerEntity: MatchPlayerEntity)

    /**
     * 매치 플레이어 영속화
     */
    @Transaction
    fun save(matchPlayerEntity: MatchPlayerEntity): MatchPlayerEntity {

        if (this.insert(matchPlayerEntity = matchPlayerEntity) == -1L) {
            this.update(matchPlayerEntity = matchPlayerEntity)
        }

        return matchPlayerEntity
    }
}