package com.monglife.mongs.data.mong.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MongDao {

    /**
     * 현재 선택된 몽 조회
     */
    @Query("SELECT * FROM mong WHERE isCurrent = true")
    fun findMongByIsCurrentTrue(): MongEntity?

    /**
     * MongId 기준 몽 조회
     */
    @Query("SELECT * FROM mong WHERE mongId = :mongId")
    fun findMongByMongId(mongId: Long): MongEntity?

    /**
     * MongId 기준 몽 Flow 조회
     */
    @Query("SELECT * FROM mong WHERE mongId = :mongId")
    fun findMongFlowByMongId(mongId: Long): Flow<MongEntity?>

    /**
     * MongId 기준 몽 Flow 목록 조회
     */
    @Query("SELECT * FROM mong")
    fun findAllMongFlow(): Flow<List<MongEntity>>

    /**
     * MongId 기준 몽 삭제
     */
    @Query("DELETE FROM mong WHERE mongId = :mongId")
    fun deleteByMongId(mongId: Long)

    /**
     * 몽 등록
     */
    @Insert(entity = MongEntity::class)
    fun insert(mongEntity: MongEntity): Long

    /**
     * 몽 수정
     */
    @Update(entity = MongEntity::class)
    fun update(mongEntity: MongEntity)

    /**
     * 몽 영속화
     */
    @Transaction
    fun save(mongEntity: MongEntity): MongEntity {

        if (this.insert(mongEntity = mongEntity) == -1L) {
            this.update(mongEntity = mongEntity)
        }

        return mongEntity
    }
}