package com.monglife.core.auth.exception

import com.monglife.core.auth.error.AuthCoreErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import java.util.Collections

/**
 * 사용자 로그인 취소 예외
 */
class GoogleLoginCanceledException(
    override val code: ErrorCode = AuthCoreErrorCode.GOOGLE_LOGIN_CANCELED,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 기기에 구글 계정 없음 예외
 */
class NoGoogleAccountException(
    override val code: ErrorCode = AuthCoreErrorCode.NO_GOOGLE_ACCOUNT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 구글 로그인 실패 예외
 *
 * Credential Manager 는 legacy GoogleSignIn 의 statusCode 같은 식별자를 주지 않아
 * 원인이 예외 타입과 메시지 문자열에만 남는다. 그 진단 정보를 result 에 실어
 * BaseViewModel 의 예외 로그(EXCEPTION >> ... - result => ...)로 흘려보낸다.
 */
class GoogleLoginException(
    override val code: ErrorCode = AuthCoreErrorCode.GOOGLE_LOGIN_FAILED,
    override val result: Map<String, Any> = Collections.emptyMap(),
    override val message: String = code.getMessage()
) : ErrorException(code = code, result = result, message = message)
