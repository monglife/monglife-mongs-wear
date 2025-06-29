package com.monglife.mongs.application.mong.exception

import com.monglife.mongs.application.mong.error.MongErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException

/**
 * 몽 옵션 조회 실패 예외
 */
class NotFoundMongOptionException(
    override val code: ErrorCode = MongErrorCode.NOT_FOUND_MONG_OPTION,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 몽 조회 실패 예외
 */
class NotFoundMongException(
    override val code: ErrorCode = MongErrorCode.NOT_FOUND_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 훈련 조회 실패 예외
 */
class NotFoundTrainingException(
    override val code: ErrorCode = MongErrorCode.NOT_FOUND_TRAINING,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 훈련 완료 실패 예외
 */
class InvalidTrainingException(
    override val code: ErrorCode = MongErrorCode.INVALID_TRAINING,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 음식 섭취 실패 예외
 */
class InvalidFeedFoodException(
    override val code: ErrorCode = MongErrorCode.INVALID_FEED_FOOD,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 간식 섭취 실패 예외
 */
class InvalidFeedSnackException(
    override val code: ErrorCode = MongErrorCode.INVALID_FEED_SNACK,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 인벤토리 소비 실패 예외
 */
class InvalidConsumeInventoryException(
    override val code: ErrorCode = MongErrorCode.INVALID_CONSUME_INVENTORY,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 랜덤 뽑기 티켓 구매 실패 예외
 */
class InvalidBuyRandomDrawTicketException(
    override val code: ErrorCode = MongErrorCode.INVALID_BUY_RANDOM_DRAW_TICKET,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 랜덤 뽑기 실패 예외
 */
class InvalidRandomDrawException(
    override val code: ErrorCode = MongErrorCode.INVALID_RANDOM_DRAW,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 몽 생성 실패 예외
 */
class InvalidCreateMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_CREATE_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 몽 삭제 실패 예외
 */
class InvalidDeleteMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_DELETE_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 쓰다듬기 실패 예외
 */
class InvalidStrokeMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_STROKE_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 수면 상태 변경 실패 예외
 */
class InvalidSleepingMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_SLEEPING_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 배변 처리 실패 예외
 */
class InvalidPoopCleanMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_POOP_CLEAN_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 진화 실패 예외
 */
class InvalidEvolutionMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_EVOLUTION_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 졸업 실패 예외
 */
class InvalidGraduateMongException(
    override val code: ErrorCode = MongErrorCode.INVALID_GRADUATE_MONG,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
