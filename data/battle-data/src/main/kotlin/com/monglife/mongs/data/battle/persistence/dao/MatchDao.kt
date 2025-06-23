package com.monglife.mongs.data.battle.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.monglife.mongs.data.battle.persistence.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    /**
     * 최근 매치 엔티티 조회
     */
    @Query("SELECT * FROM `match` ORDER BY createdAt DESC LIMIT 1")
    fun findTopMatch(): MatchEntity?

    /**
     * QueueId 기준 매치 엔티티 조회
     */
    @Query("SELECT * FROM `match` WHERE queueId = :queueId")
    fun findMatchByQueueId(queueId: String): MatchEntity?

    /**
     * MatchId 기준 매치 엔티티 조회
     */
    @Query("SELECT * FROM `match` WHERE matchId = :matchId")
    fun findMatchByMatchId(matchId: Long): MatchEntity?

    /**
     * QueueId 기준 매치 라이브 엔티티 조회
     */
    @Query("SELECT * FROM `match` WHERE queueId = :queueId")
    fun findMatchLiveByQueueId(queueId: String): Flow<MatchEntity?>

    /**
     * MatchId 기준 매치 라이브 엔티티 조회
     */
    @Query("SELECT * FROM `match` WHERE matchId = :matchId")
    fun findMatchLiveByMatchId(matchId: Long): Flow<MatchEntity?>

    /**
     * 모든 매치 삭제
     */
    @Query("DELETE FROM `match`")
    fun deleteAll()

    /**
     * 매치 등록
     */
    @Insert(entity = MatchEntity::class)
    fun insert(matchEntity: MatchEntity): Long

    /**
     * 매치 수정
     */
    @Update(entity = MatchEntity::class)
    fun update(matchEntity: MatchEntity)

    /**
     * 매치 영속화
     */
    @Transaction
    fun save(matchEntity: MatchEntity): MatchEntity {

        if (this.insert(matchEntity = matchEntity) == -1L) {
            this.update(matchEntity = matchEntity)
        }

        return matchEntity
    }
}