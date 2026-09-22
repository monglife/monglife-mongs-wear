package com.monglife.core.auth.vo

/**
 * 구글 로그인 결과 Vo
 *
 * googleAccountId 는 ID 토큰의 sub 클레임이다.
 * legacy GoogleSignInAccount.id 와 같은 값이라 서버의 socialAccountId 계약이 그대로 유지된다.
 */
data class GoogleAccountVo(
    val googleAccountId: String,
    val email: String,
    val displayName: String?,
    val idToken: String,
)
