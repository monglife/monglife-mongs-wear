package com.monglife.mongs.data.mong.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MongDao {

    /**
     * MongIds 기준 MongId 조회
     */
    @Query("SELECT mongId FROM mong WHERE mongId NOT IN (:mongIds)")
    fun findAllNotExistsMongIds(mongIds: List<Long>): List<Long>

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
     * MongId 기준 몽 삭제
     */
    @Query("DELETE FROM mong WHERE mongId = :mongId")
    fun deleteMongByMongId(mongId: Long)

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
        this.findMongByMongId(mongId = mongEntity.mongId)?.let {
            this.update(mongEntity = mongEntity)
        } ?: run {
            this.insert(mongEntity = mongEntity)
        }

        return mongEntity
    }

    /**
     * MongId 기준 몽 옵션 조회
     */
    @Query("SELECT * FROM mong_option WHERE mongId = :mongId")
    fun findMongOptionByMongId(mongId: Long): MongOptionEntity?

    /**
     * MongId 기준 몽 옵션 조회
     */
    @Query("SELECT * FROM mong_option WHERE mongId = :mongId")
    fun findMongOptionFlowByMongId(mongId: Long): Flow<MongOptionEntity?>

    /**
     * MongId 기준 몽 옵션 삭제
     */
    @Query("DELETE FROM mong_option WHERE mongId = :mongId")
    fun deleteMongOptionByMongId(mongId: Long)

    /**
     * 몽 옵션 등록
     */
    @Insert(entity = MongOptionEntity::class)
    fun insert(mongOptionEntity: MongOptionEntity): Long

    /**
     * 몽 옵션 수정
     */
    @Update(entity = MongOptionEntity::class)
    fun update(mongOptionEntity: MongOptionEntity)

    /**
     * 몽 옵션 영속화
     */
    @Transaction
    fun save(mongOptionEntity: MongOptionEntity): MongOptionEntity {
        this.findMongOptionByMongId(mongId = mongOptionEntity.mongId)?.let {
            this.update(mongOptionEntity = mongOptionEntity)
        } ?: run {
            this.insert(mongOptionEntity = mongOptionEntity)
        }

        return mongOptionEntity
    }
}