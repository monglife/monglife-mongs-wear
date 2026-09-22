package com.monglife.core.data.global

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * 디버그 빌드 여부
 *
 * 진단용 로그는 요청 헤더 · 응답 바디 · MQTT 페이로드 전문을 그대로 출력하므로
 * 릴리스에서는 아예 실행되지 않아야 한다.
 *
 * R8 의 `-assumenosideeffects` 만으로는 부족하다. 그 지시어는 "호출을 지워도 된다" 는
 * 뜻이라 인자 계산에 부수효과가 없다고 증명될 때만 함께 사라진다.
 * 예를 들어 MqttLogConsumer 는 로그 인자를 만들려고 gson.fromJson 을 부르는데,
 * R8 은 그게 부수효과가 없음을 증명할 수 없어 파싱 코드를 남긴다.
 * 그래서 R8 에 기대지 않고 코드로 막는다.
 */
internal fun Context.isDebuggable(): Boolean =
    (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
