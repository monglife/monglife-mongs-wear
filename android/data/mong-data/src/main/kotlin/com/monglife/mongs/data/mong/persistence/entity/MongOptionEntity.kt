package com.monglife.mongs.data.mong.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.mong.model.MongOption

@Entity("mong_option")
data class MongOptionEntity(
    @PrimaryKey
    val mongId: Long,
    val graduateCheck: Boolean,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): MongOption = MongOption(
        mongId = this.mongId,
        graduateCheck = this.graduateCheck,
    )
}