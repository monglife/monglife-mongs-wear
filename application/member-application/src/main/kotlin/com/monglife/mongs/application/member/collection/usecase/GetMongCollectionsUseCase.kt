package com.monglife.mongs.domain.member.collection.usecase

import com.monglife.mongs.domain.member.collection.repository.CollectionRepository
import com.monglife.mongs.domain.member.collection.vo.MongCollectionVo
import com.mongs.wear.core.exception.data.GetMongCollectionsException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetMongCollectionUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 컬렉션 목록 조회 UseCase
 */
class GetMongCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository,
) : BaseNoParamUseCase<List<MongCollectionVo>>() {

    override suspend fun execute(): List<MongCollectionVo> {
        return withContext(Dispatchers.IO) {
            collectionRepository.getMongCollections().map {
                MongCollectionVo(
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
            is GetMongCollectionsException -> throw GetMongCollectionUseCaseException()

            else -> throw GetMongCollectionUseCaseException()
        }
    }
}