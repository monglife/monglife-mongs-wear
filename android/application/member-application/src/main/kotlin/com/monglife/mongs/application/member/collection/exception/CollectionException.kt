package com.monglife.mongs.application.member.collection.exception

import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import com.monglife.mongs.application.member.collection.error.CollectionErrorCode

/**
 * 맵 탐색 실패 예외
 */
class InvalidSearchCollectionMapException(
    override val code: ErrorCode = CollectionErrorCode.INVALID_SEARCH_COLLECTION_MAP,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)