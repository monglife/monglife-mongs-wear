# core:data-core consumer rules
#
# 이 파일은 라이브러리 consumer rules 라 앱 모듈로 자동 병합되며,
# app 모듈의 proguard-rules.pro 와 달리 copyPrivate* 태스크가 덮어쓰지 않는다.
# (copyPrivate* 는 app 디렉터리의 *.pro 를 지우고 configs 것으로 교체한다)

# ---------------------------------------------------------------------------
# Gson — DTO/VO/도메인 모델은 필드명을 리플렉션으로 읽으므로 난독화하면 파싱이 깨진다.
# ---------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Gson 이 역직렬화하는 타입 (HTTP 응답 DTO, MQTT 이벤트 DTO, 요청 DTO)
-keep class com.monglife.core.data.web.dto.** { *; }
-keep class com.monglife.mongs.data.**.dto.** { *; }
-keep class com.monglife.mongs.data.**.web.client.request.** { *; }
-keep class com.monglife.mongs.data.**.web.client.response.** { *; }

# 도메인 모델 / VO (enum 이름이 그대로 직렬화 값으로 쓰인다)
-keep class com.monglife.mongs.domain.** { *; }
-keep class com.monglife.mongs.application.**.vo.** { *; }

# enum 의 values()/valueOf() 는 리플렉션으로 호출된다
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Gson TypeAdapter / TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---------------------------------------------------------------------------
# Retrofit — 인터페이스의 제네릭 시그니처와 애노테이션이 필요하다.
# (Retrofit 자체 consumer rules 가 있지만 우리 클라이언트 인터페이스는 명시해 둔다)
# ---------------------------------------------------------------------------
-keep,allowobfuscation interface com.monglife.core.data.web.client.** { *; }
-keep,allowobfuscation interface com.monglife.mongs.data.**.web.client.** { *; }

# ---------------------------------------------------------------------------
# MQTT (paho) — 서비스/리플렉션 기반이라 통째로 유지한다.
# ---------------------------------------------------------------------------
-keep class org.eclipse.paho.client.mqttv3.** { *; }
-keep class info.mqtt.android.service.** { *; }
-dontwarn org.eclipse.paho.**
-dontwarn info.mqtt.android.service.**
