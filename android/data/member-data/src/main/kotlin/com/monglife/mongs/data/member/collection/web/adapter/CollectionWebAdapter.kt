package com.monglife.mongs.data.member.collection.web.adapter

import android.location.LocationManager
import com.monglife.mongs.application.member.collection.exception.InvalidSearchCollectionMapException
import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMapResponse
import com.monglife.mongs.application.member.collection.port.web.response.GetCollectionMongResponse
import com.monglife.mongs.data.member.collection.web.client.CollectionWebClient
import com.monglife.mongs.data.member.collection.web.client.request.SearchCollectionMapRequestDto
import com.monglife.mongs.data.member.collection.web.manager.LocationSensorManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionWebAdapter @Inject constructor(
    private val collectionWebClient: CollectionWebClient,
    private val locationSensorManager: LocationSensorManager
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

    /**
     * 맵 탐색
     */
    override suspend fun searchCollectionMap(): GetCollectionMapResponse? {

        val locationVo = locationSensorManager.getLocation()

        return collectionWebClient.searchCollectionMap(
            searchCollectionMapRequestDto = SearchCollectionMapRequestDto(
                latitude = locationVo?.latitude ?: 0.0,
                longitude = locationVo?.longitude ?: 0.0,
            )
        ).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()
                ?: throw InvalidSearchCollectionMapException()

            val data = body.takeIf { it.result.isFound }?.result?.data

            data?.let {
                GetCollectionMapResponse(
                    mapCode = it.mapCode,
                    mapName = it.mapName,
                    isIncluded = it.isIncluded,
                )
            }
        }
    }
}