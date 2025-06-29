package com.monglife.mongs.data.core.web.constant

object HttpConst {
    const val AUTHORIZATION_HEADER = "Authorization"
    const val INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE = 500
    const val INTERNAL_SERVER_ERROR_HTTP_MESSAGE = "서버 연결 실패"
    const val FORBIDDEN_HTTP_STATUS_CODE = 401
    const val FORBIDDEN_MESSAGE = "권한 인증 실패"
    const val AUTHORIZATION_HTTP_STATUS_CODE = 403
    const val AUTHORIZATION_MESSAGE = "권한 인가 실패"
}