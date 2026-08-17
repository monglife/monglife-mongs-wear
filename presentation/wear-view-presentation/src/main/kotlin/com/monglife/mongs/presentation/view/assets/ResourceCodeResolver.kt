package com.monglife.mongs.presentation.view.assets

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ResourceCode"

/**
 * 이미 경고를 남긴 (enum, code) 조합
 * 컴포지션 경로에서 호출되므로 같은 코드로 로그가 반복되지 않게 한다.
 */
private val warnedCodes = ConcurrentHashMap.newKeySet<String>()

/**
 * 서버가 준 코드 문자열로 리소스 enum 상수를 찾는다. 찾지 못하면 폴백을 쓴다.
 *
 * 이 조회는 enum 상수 **이름**에 의존한다. R8 난독화가 이름을 건드리면 모든 조회가
 * 실패하는데, 예외를 그냥 삼키면 캐릭터·배경·아이콘이 빈 이미지가 되면서도 아무 신호가
 * 남지 않는다. 그래서 폴백을 쓸 때 반드시 로그를 남긴다.
 * (이름 보존 keep 룰은 proguard/mongs-release.pro 에 있다)
 *
 * 서버가 클라이언트보다 새로운 코드를 내려주는 정상 상황에서도 폴백을 타므로,
 * 로그 레벨은 경고까지만 쓴다.
 */
internal fun <T : Enum<T>> resolveResourceCode(
    enumName: String,
    code: String,
    fallback: T,
    valueOf: (String) -> T,
): T = runCatching { valueOf(code) }.getOrElse {
    if (warnedCodes.add("$enumName:$code")) {
        Log.w(TAG, "알 수 없는 $enumName 코드 => \"$code\" (폴백 ${fallback.name} 사용)")
    }

    fallback
}
