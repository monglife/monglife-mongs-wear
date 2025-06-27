package com.monglife.mongs.application.member.player.exception

import com.monglife.mongs.application.member.player.error.PlayerErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException

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

/**
 *  플레이어 등록 실패 예외
 */
class InvalidCreatePlayerException(
    override val code: ErrorCode = PlayerErrorCode.INVALID_CREATE_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 *  플레이어 조회 실패 예외
 */
class NotFoundPlayerException(
    override val code: ErrorCode = PlayerErrorCode.NOT_FOUND_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 슬롯 구매 실패 예외
 */
class InvalidBuySlotException(
    override val code: ErrorCode = PlayerErrorCode.INVALID_BUY_SLOT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 스타 포인트 환전 실패 예외
 */
class InvalidExchangeStarPointException(
    override val code: ErrorCode = PlayerErrorCode.INVALID_EXCHANGE_STAR_POINT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
