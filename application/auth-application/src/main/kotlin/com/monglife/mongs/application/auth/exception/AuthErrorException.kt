package com.monglife.mongs.application.auth.exception

import com.monglife.mongs.application.auth.error.AuthErrorCode
import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException

/**
 * 앱 버전 검증 실패 예외
 */
class VerifyAppVersionException(
    override val code: ErrorCode = AuthErrorCode.VERIFY_APP_VERSION,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 회원 가입 필요 예외
 */
class NeedJoinException(
    override val code: ErrorCode = AuthErrorCode.NEED_JOIN,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 회원 가입 실패 예외
 */
class InvalidJoinException(
    override val code: ErrorCode = AuthErrorCode.INVALID_JOIN,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 로그인 실패 예외
 */
class InvalidLoginException(
    override val code: ErrorCode = AuthErrorCode.INVALID_LOGIN,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 로그 아웃 실패 예외
 */
class InvalidLogoutException(
    override val code: ErrorCode = AuthErrorCode.INVALID_LOGOUT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 *  기기 등록 실패 예외
 */
class InvalidCreateUserDeviceException(
    override val code: ErrorCode = AuthErrorCode.INVALID_CREATE_USER_DEVICE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)