package com.monglife.mongs.application.member.collection.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.vo.CollectionMapVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 맵 컬렉션 목록 조회 UseCase
 */
class GetCollectionMapsUseCase @Inject constructor(
    private val collectionWebPort: CollectionWebPort,
) : BaseNoParamUseCase<List<CollectionMapVo>>() {

    override suspend fun execute(): List<CollectionMapVo> {
        return withContext(Dispatchers.IO) {
            collectionWebPort.getCollectionMaps().map { response ->
                CollectionMapVo.of(collectionMap = response.toDomain())
            }
        }
    }
}