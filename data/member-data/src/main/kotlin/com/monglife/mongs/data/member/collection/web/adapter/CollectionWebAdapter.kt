package com.monglife.mongs.data.member.collection.web.adapter

import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMapResponse
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMongResponse
import com.monglife.mongs.data.member.collection.web.client.CollectionWebClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CollectionWebAdapterModule {
    @Binds
    @Singleton
    abstract fun bindCollectionWebPort(adapter: CollectionWebAdapter): CollectionWebPort
}

@Singleton
class CollectionWebAdapter @Inject constructor(
    private val collectionWebClient: CollectionWebClient,
) : CollectionWebPort {

    /**
     * 컬렉션 맵 목록 조회
     */
    override suspend fun getCollectionMaps(): List<GetCollectionMapResponse> =
        collectionWebClient.getCollectionMaps().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetCollectionMapResponse(
                    mapCode = it.mapCode,
                    mapName = it.mapName,
                    isIncluded = it.isIncluded,
                )
            } ?: emptyList()
        }

    /**
     * 컬렉션 몽 목록 조회
     */
    override suspend fun getCollectionMongs(): List<GetCollectionMongResponse> =
        collectionWebClient.getCollectionMongs().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetCollectionMongResponse(
                    mongCode = it.mongCode,
                    mongName = it.mongName,
                    isIncluded = it.isIncluded,
                )
            } ?: emptyList()
        }
}