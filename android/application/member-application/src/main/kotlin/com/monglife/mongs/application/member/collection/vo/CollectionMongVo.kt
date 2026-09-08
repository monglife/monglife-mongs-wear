package com.monglife.mongs.application.member.collection.vo

import com.monglife.mongs.domain.member.collection.model.CollectionMong

data class CollectionMongVo(
    val code: String,
    val name: String,
    val isIncluded: Boolean,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(collectionMong: CollectionMong) = CollectionMongVo(
            code = collectionMong.code,
            name = collectionMong.name,
            isIncluded = collectionMong.isIncluded,
        )
    }
}