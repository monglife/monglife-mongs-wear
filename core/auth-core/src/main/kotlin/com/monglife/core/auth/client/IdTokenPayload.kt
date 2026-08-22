package com.monglife.core.auth.client

import android.util.Base64
import org.json.JSONObject

/**
 * ID 토큰(JWT) 의 payload
 *
 * Gson DTO 를 쓰지 않는다. Gson 은 필드를 리플렉션으로 읽어 R8 keep 룰이 필요해지고,
 * 그 룰 누락으로 릴리스 로그인이 깨진 이력이 있다(core/data-core/consumer-rules.pro 참고).
 * JSONObject 는 리플렉션이 아니라 축소·난독화의 영향을 받지 않는다.
 *
 * 서명 검증은 하지 않는다. 클라이언트가 자기에게 온 토큰을 검증하는 것은 의미가 없고,
 * 검증은 idToken 을 함께 전송받는 서버의 몫이다.
 */
internal data class IdTokenPayload(
    val sub: String,
    val email: String,
    val name: String?,
) {
    companion object {

        /**
         * JWT 의 두 번째 세그먼트(payload)를 디코딩한다.
         *
         * JWT 는 padding 없는 base64url 이라 URL_SAFE + NO_PADDING 이 필요하다.
         * 형식이 어긋나면 null 을 돌려주고 호출부가 로그인 실패로 처리한다.
         */
        fun of(idToken: String): IdTokenPayload? = runCatching {
            val segments = idToken.split(".")

            if (segments.size < 2) return@runCatching null

            val payload = String(
                Base64.decode(segments[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )

            JSONObject(payload).let { json ->
                IdTokenPayload(
                    sub = json.getString("sub"),
                    email = json.getString("email"),
                    name = json.optString("name").ifEmpty { null },
                )
            }
        }.getOrNull()
    }
}
