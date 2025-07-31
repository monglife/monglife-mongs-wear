package com.monglife.mongs.application.member.collection.vo

import com.monglife.mongs.domain.member.collection.model.CollectionMap

data class CollectionMapVo(
    val code: String,
    val name: String,
    val isIncluded: Boolean,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(collectionMap: CollectionMap) = CollectionMapVo(
            code = collectionMap.code,
            name = collectionMap.name,
            isIncluded = collectionMap.isIncluded,
        )
    }
}