package com.monglife.mongs.application.battle.exception

import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import com.monglife.mongs.application.battle.error.BattleErrorCode

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
