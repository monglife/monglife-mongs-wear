package com.monglife.mongs.domain.member.collection.usecase

import com.monglife.mongs.domain.member.collection.repository.CollectionRepository
import com.monglife.mongs.domain.member.collection.vo.MapCollectionVo
import com.mongs.wear.core.exception.data.GetMapCollectionsException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetMapCollectionsUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 맵 컬렉션 목록 조회 UseCase
 */
class GetMapCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository,
) : BaseNoParamUseCase<List<MapCollectionVo>>() {

    override suspend fun execute(): List<MapCollectionVo> {
        return withContext(Dispatchers.IO) {
            collectionRepository.getMapCollections().map {
                MapCollectionVo(
                    code = it.code,
                    name = it.name,
                    isIncluded = it.isIncluded
                )
            }
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetMapCollectionsException -> throw GetMapCollectionsUseCaseException()

            else -> throw GetMapCollectionsUseCaseException()
        }
    }
}