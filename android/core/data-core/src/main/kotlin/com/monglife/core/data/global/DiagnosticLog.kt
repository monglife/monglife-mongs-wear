package com.monglife.core.data.global

import android.os.Process
import java.io.BufferedReader

/**
 * 오류 신고에 붙일 진단 로그를 모은다.
 *
 * ## 왜 링 버퍼가 아니라 logcat 인가
 *
 * 앱 안에 버퍼를 두면 우리가 남긴 것만 담긴다. logcat 을 읽으면 OkHttp·MQTT·
 * Health Services 같은 라이브러리가 남긴 것과 시스템이 남긴 것까지 들어온다.
 * 정작 원인이 라이브러리 쪽일 때가 많아 그쪽이 더 쓸모 있다.
 * 호출부를 서른 곳 넘게 고칠 필요도 없다.
 *
 * 젤리빈 이후 앱은 **자기 UID 의 로그만** 읽을 수 있고, 여기엔 권한이 필요 없다.
 * `--pid` 로 한 번 더 좁혀 같은 UID 의 다른 프로세스까지 섞이지 않게 한다.
 *
 * ## 릴리스에서 무엇이 잡히는가
 *
 * `proguard/mongs-release.pro` 가 `Log.i/d/v` 를 지우므로 성공 경로는 비어 있고
 * **`Log.w`(17곳)·`Log.e`(3곳)만 남는다.** 즉 이 로그는 사실상 "실패 기록"이다.
 * 신고할 만한 상황이면 그게 남아 있다.
 *
 * 아무 문제 없이 쓴 직후에 신고하면 몇 줄뿐이거나 비어 있을 수 있다. 그게 정상이다 -
 * 남길 실패가 없었다는 뜻이다.
 */
object DiagnosticLog {

    /** 마지막 이 줄 수만 보낸다. 오래된 줄은 신고 시점과 무관하다. */
    private const val MAX_LINES = 300

    /** 서버 `content` 가 5,000자인 것과 같은 급으로 맞춘다. 초과분은 앞에서 자른다. */
    private const val MAX_CHARS = 8_000

    /** logcat 이 응답하지 않을 때 신고 자체를 막지 않는다. */
    private const val TIMEOUT_MILLIS = 2_000L

    /**
     * logcat 필터. **이게 없으면 이 기능은 사실상 동작하지 않는다.**
     *
     * 처음에는 필터 없이 마지막 300줄을 그대로 보냈는데, 실기기에서 받아 보니 60줄이
     * 전부 `VRI[MainActivity]`(터치 이벤트)·`ImeTracker`·`InputMethodManager` 였다.
     * **신고를 쓰려고 키보드를 두드리는 행위 자체가** IME 로그를 쏟아내서, 정작 원인이었을
     * `Log.w`/`Log.e` 를 상한 밖으로 밀어낸다. 우리 태그는 한 줄도 남지 않았다.
     *
     * `*:W` 로 경고 이상만 받는다. BaseViewModel 의 미처리 예외 로그는 태그가 ViewModel
     * 클래스명이라 고정 태그로 못 거는데, 그게 `Log.e` 라 이 필터에 그대로 걸린다.
     *
     * 그래도 남는 잡음 넷은 태그를 침묵(`:S`)시킨다. `HWUI`(이미지 디코딩 경고)와
     * `ComponentDiscovery`(Firebase ktx 레지스트라 탐색)가 특히 많아, 실측에서 60줄 중
     * 49줄을 이 둘이 차지했다.
     *
     * 부수 효과가 하나 있다. `HttpLogInterceptor` 의 토큰 줄은 `Log.i` 라 이제 아예
     * 안 들어온다 - 디버그 빌드에서도 그렇다. 아래 마스킹은 그래서 1차 방어가 아니라
     * 마지막 보루가 됐다. 둘 다 둔다.
     */
    private val LOGCAT_ARGS = listOf(
        "logcat", "-d", "-v", "time",
        "HWUI:S",
        "ComponentDiscovery:S",
        "RemoteInputConnectionImpl:S",
        "InteractionJankMonitor:S",
        "*:W",
    )

    /**
     * 가려야 하는 것들.
     *
     * `HttpLogInterceptor` 가 `Bearer` 토큰 전문을 찍는다. 릴리스에서는 그 줄이 `Log.i` 라
     * 지워지지만, **디버그 빌드로 신고하면 토큰이 그대로 서버 DB 에 들어가고 관리자 화면에 뜬다.**
     * 지금은 안 샌다는 이유로 빼 두면, 나중에 로그 정책이 한 번 바뀔 때 조용히 새기 시작한다.
     */
    private val REDACTIONS = listOf(
        Regex("""Bearer\s+[A-Za-z0-9._\-]+""") to "Bearer ***",
        // 헤더를 거치지 않고 본문에 실려 오는 토큰(재발급 응답 등)까지 잡는다
        Regex("""eyJ[A-Za-z0-9._\-]{20,}""") to "***",
        Regex("""("?(?:accessToken|refreshToken|password)"?\s*[:=]\s*)"?[^,\s"}]+""") to "$1***",
    )

    /**
     * 실패하면 null 을 준다. 진단 로그를 못 모았다고 신고가 막히면 안 된다.
     */
    fun collect(): String? = runCatching {
        // --pid 는 태그 필터보다 앞에 와야 한다. 뒤에 붙이면 logcat 이 필터 표현식으로 읽는다.
        val process = ProcessBuilder(
            LOGCAT_ARGS.toMutableList().apply { add(4, "--pid=${Process.myPid()}") },
        ).redirectErrorStream(true).start()

        val raw = try {
            process.inputStream.bufferedReader().use(BufferedReader::readLines)
        } finally {
            // waitFor 에 기대지 않는다 - logcat 이 안 끝나면 신고가 그 자리에서 멈춘다
            if (!process.waitFor(TIMEOUT_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                process.destroy()
            }
        }

        raw.takeLast(MAX_LINES)
            .joinToString("\n")
            .let { REDACTIONS.fold(it) { acc, (pattern, replacement) -> pattern.replace(acc, replacement) } }
            .takeLast(MAX_CHARS)
            .ifBlank { null }
    }.getOrNull()
}
