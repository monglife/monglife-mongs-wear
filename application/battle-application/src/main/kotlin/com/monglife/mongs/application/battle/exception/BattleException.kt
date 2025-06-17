package com.monglife.mongs.application.battle.exception

import com.monglife.mongs.application.battle.error.BattleErrorCode
import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException

/**
 * 매치 큐 등록 실패 예외
 */
class InvalidCreateQueuePlayerException(
    override val code: ErrorCode = BattleErrorCode.INVALID_CREATE_QUEUE_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 큐 삭제 실패 예외
 */
class InvalidDeleteQueuePlayerException(
    override val code: ErrorCode = BattleErrorCode.INVALID_DELETE_QUEUE_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 보상 조회 실패 예외
 */
class NotFoundMatchRewardException(
    override val code: ErrorCode = BattleErrorCode.NOT_FOUND_MATCH_REWARD,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 승리 매치 플레이어 조회 실패 예외
 */
class NotFoundWinnerMatchPlayerException(
    override val code: ErrorCode = BattleErrorCode.NOT_FOUND_WINNER_MATCH_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 조회 실패 예외
 */
class NotFoundMatchException(
    override val code: ErrorCode = BattleErrorCode.NOT_FOUND_MATCH,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 플레이어 조회 실패 예외
 */
class NotFoundMatchPlayerException(
    override val code: ErrorCode = BattleErrorCode.NOT_FOUND_MATCH_PLAYER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 입장 이벤트 전송 실패 예외
 */
class InvalidPublishMatchEnterException(
    override val code: ErrorCode = BattleErrorCode.INVALID_PUBLISH_MATCH_ENTER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 퇴장 이벤트 전송 실패 예외
 */
class InvalidPublishMatchExitException(
    override val code: ErrorCode = BattleErrorCode.INVALID_PUBLISH_MATCH_EXIT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 선택 이벤트 전송 실패 예외
 */
class InvalidPublishMatchPickException(
    override val code: ErrorCode = BattleErrorCode.INVALID_PUBLISH_MATCH_PICK,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 구독 실패 예외
 */
class InvalidSubscribeMatchException(
    override val code: ErrorCode = BattleErrorCode.INVALID_SUBSCRIBE_MATCH,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 구독 해제 실패 예외
 */
class InvalidDisSubscribeMatchException(
    override val code: ErrorCode = BattleErrorCode.INVALID_DIS_SUBSCRIBE_MATCH,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 큐 구독 실패 예외
 */
class InvalidSubscribeQueueException(
    override val code: ErrorCode = BattleErrorCode.INVALID_SUBSCRIBE_QUEUE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 매치 큐 구독 해제 실패 예외
 */
class InvalidDisSubscribeQueueException(
    override val code: ErrorCode = BattleErrorCode.INVALID_DIS_SUBSCRIBE_QUEUE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)