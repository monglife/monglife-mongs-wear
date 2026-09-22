# data:device-data consumer rules
#
# 라이브러리 consumer rules 라 앱 모듈로 자동 병합된다.
# (core:data-core/consumer-rules.pro 와 같은 구조 — 그쪽은 Gson/Retrofit/MQTT 담당)

# ---------------------------------------------------------------------------
# Health Services — protobuf-lite 리플렉션
#
# androidx.health.services.client.proto.DataProto$* 는 셰이딩되지 않은
# com.google.protobuf.GeneratedMessageLite 를 상속한다. protobuf-lite 런타임은
# dynamicMethod 의 스키마 문자열로 필드를 **이름으로** 찾으므로, R8 이 필드를
# 난독화하면 런타임에 터진다.
#
#   W/StepCollection: capability 조회 실패
#   java.lang.RuntimeException: Field packageName_ for b13 not found.
#     Known fields are [public java.lang.String b13.e, ...]
#
# health-services-client 1.0.0 AAR 은 proguard.txt 를 싣지 않아 라이브러리에서
# 룰이 내려오지 않는다. 여기서 직접 막는다.
#
# 이 룰이 없으면 resolveSupportedSource() 가 예외를 먹고 null 을 돌려주고,
# 수집 경로가 조용히 SENSOR 로 내려간다. 즉 **릴리스에서는 passive monitoring 이
# 통째로 죽고** 앱이 꺼진 동안의 걸음을 밀어 주는 경로가 사라진다.
# 디버그 빌드는 R8 을 안 돌려 멀쩡해 보이므로 실기기 릴리스 빌드로만 드러난다.
# (2.3.1 실기기 검증에서 발견. 그 전 스토어 빌드도 전부 해당된다.)
#
# 앱 전역 룰인 이유: DataStore 등 다른 protobuf-lite 사용처도 같은 위험을 진다.
# ---------------------------------------------------------------------------
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
