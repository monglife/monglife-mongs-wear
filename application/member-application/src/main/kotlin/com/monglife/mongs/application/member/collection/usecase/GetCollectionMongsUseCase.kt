package com.monglife.mongs.application.member.collection.usecase

import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.vo.CollectionMongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 컬렉션 목록 조회 UseCase
 */
class GetCollectionMongsUseCase @Inject constructor(
    private val collectionWebPort: CollectionWebPort,
) : BaseNoParamUseCase<List<CollectionMongVo>>() {

    override suspend fun execute(): List<CollectionMongVo> {
        return withContext(Dispatchers.IO) {
            // 몽 컬렉션 목록 조회 요청
            collectionWebPort.getCollectionMongs().map { response ->
                // CollectionMongVo 반환
                CollectionMongVo.of(collectionMong = response.toDomain())
            }
        }
    }
}