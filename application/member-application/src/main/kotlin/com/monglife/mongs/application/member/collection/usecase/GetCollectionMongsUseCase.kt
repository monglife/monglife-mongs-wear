package com.monglife.mongs.application.member.collection.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.vo.CollectionMongVo
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
            collectionWebPort.getCollectionMongs().map { response ->
                CollectionMongVo.of(collectionMong = response.toDomain())
            }
        }
    }
}