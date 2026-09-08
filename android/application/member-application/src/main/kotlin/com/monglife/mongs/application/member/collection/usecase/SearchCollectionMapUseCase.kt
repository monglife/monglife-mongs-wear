package com.monglife.mongs.application.member.collection.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.collection.exception.InvalidSearchCollectionMapException
import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.vo.CollectionMapVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 맵 탐색 UseCase
 */
class SearchCollectionMapUseCase @Inject constructor(
    private val collectionWebPort: CollectionWebPort,
) : BaseNoParamUseCase<CollectionMapVo?>() {

    @Throws(InvalidSearchCollectionMapException::class)
    override suspend fun execute(): CollectionMapVo? {
        return withContext(Dispatchers.IO) {
            collectionWebPort.searchCollectionMap().let { response ->
                response?.let {
                    CollectionMapVo(
                        code = response.mapCode,
                        name = response.mapName,
                        isIncluded = response.isIncluded,
                    )
                }
            }
        }
    }
}