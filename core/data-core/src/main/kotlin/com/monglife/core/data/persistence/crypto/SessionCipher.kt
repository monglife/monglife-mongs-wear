package com.monglife.core.data.persistence.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션 토큰 암복호화
 *
 * 키는 Android Keystore(TEE)에 있고 앱 밖으로 나오지 않는다.
 * 막을 수 있는 것: 루팅·부트로더 언락 기기나 저장소 덤프에서의 오프라인 추출.
 * 막지 못하는 것: 앱 프로세스에 붙을 수 있는 공격자(Cipher 를 그대로 호출하면 된다).
 *
 * androidx.security:security-crypto 는 deprecated 라 쓰지 않고 Keystore 를 직접 쓴다.
 */
@Singleton
class SessionCipher @Inject constructor() {

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
        private const val ALIAS = "mongs.session.v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val GCM_IV_BYTES = 12

        /**
         * 암호문 판별용 접두사
         * JWT 는 base64url 이라 "eyJ" 로 시작하므로 이 접두사와 겹칠 수 없다.
         * 덕분에 별도 스키마 버전 키 없이 평문 레거시 값과 구분된다.
         */
        const val MARKER = "enc:v1:"
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey?.let { return it }

        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            /**
             * 사용자 인증·잠금 해제를 요구하면 안 된다.
             * AuthorizationInterceptor 가 모든 HTTP 요청에서 세션을 읽고
             * SyncStepWorker 가 백그라운드에서 세션을 읽는다.
             * 요구를 걸면 화면이 잠긴 상태나 워치를 벗은 상태에서 전부 실패한다.
             */
            .setUserAuthenticationRequired(false)
            // IV 재사용 금지 (기본값이지만 의도를 명시한다)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            .apply { init(spec) }
            .generateKey()
    }

    /**
     * 평문 → "enc:v1:" + Base64(IV || ciphertext || GCM tag)
     *
     * IV 는 Keystore 프로바이더가 생성한 것을 그대로 쓴다.
     * setRandomizedEncryptionRequired(true) 때문에 IV 를 직접 지정하면 예외가 난다.
     */
    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv

        return MARKER + Base64.encodeToString(iv + body, Base64.NO_WRAP)
    }

    /**
     * 복호화. 실패하면 null 을 돌려주고 절대 예외를 던지지 않는다.
     *
     * 기기 복원으로 Keystore 키가 사라졌거나 OEM 버그로 키를 못 읽는 경우가 있는데,
     * 그때 앱이 죽는 대신 "세션 없음" 으로 떨어져 로그인 화면으로 가야 한다.
     */
    fun decrypt(stored: String): String? = runCatching {
        val raw = Base64.decode(stored.removePrefix(MARKER), Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(GCM_TAG_BITS, raw, 0, GCM_IV_BYTES),
            )
        }

        String(
            cipher.doFinal(raw, GCM_IV_BYTES, raw.size - GCM_IV_BYTES),
            Charsets.UTF_8,
        )
    }.getOrNull()
}
