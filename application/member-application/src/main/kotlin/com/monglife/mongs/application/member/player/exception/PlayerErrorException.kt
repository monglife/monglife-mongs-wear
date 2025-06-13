package com.monglife.mongs.application.auth.exception

import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException
import com.monglife.mongs.domain.member.collection.error.PlayerErrorCode

/**
 * 플레이어 구독 실패 예외
 */
class InvalidSubscribePlayerException(
    override val code: ErrorCode = PlayerErrorCode.INVALID_SUBSCRIBE_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 플레이어 구독 해제 실패 예외
 */
class InvalidDisSubscribePlayerException(
    override val code: ErrorCode = PlayerErrorCode.INVALID_DIS_SUBSCRIBE_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
