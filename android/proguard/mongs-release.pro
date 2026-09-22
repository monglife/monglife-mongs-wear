# 앱 전역 R8 룰 (wear / mobile 공용)
#
# 이 파일이 app 모듈이 아니라 저장소 루트에 있는 이유:
# 루트 build.gradle 의 copyPrivateDev/Stage/Prd 태스크가
#   project.delete fileTree("./") { include("*.pro") }
# 로 app 디렉터리의 *.pro 를 매번 삭제하고 configs 서브모듈 것으로 교체한다.
# (configs 가 주는 proguard-rules.pro 는 0바이트 빈 파일이다)
# 루트는 그 태스크의 대상이 아니라서 안전하다.
#
# 라이브러리 리플렉션 keep 룰(Gson DTO / Retrofit / MQTT)은
# core/data-core/consumer-rules.pro 에 있다. 여기에는 앱 전역 정책만 둔다.

# ---------------------------------------------------------------------------
# 리소스 코드 enum 의 상수 이름 보존
#
# MongResourceCode / MapResourceCode / FoodResourceCode / SnackResourceCode /
# TrainingResourceCode / HelpResourceCode 는 서버가 준 문자열로
# valueOf(code) 를 호출해 상수를 찾는다. 상수 이름이 바뀌면 조회가 전부 실패하고
# 캐릭터 · 배경 · 아이콘이 빈 이미지가 된다.
#
# AGP 기본 규칙은 enum 의 values() / valueOf() "메서드"만 keep 한다. 상수 자체는 지켜주지 않는다.
#
# 실측(usage.txt): 이 룰이 없으면 R8 이 폴백 상수(CH444 등)만 남기고
# CH000 ~ CH335 를 전부 "제거" 한다. 정적 참조가 폴백뿐이라 나머지가 죽은 코드로 판정되기 때문이다.
# 이름 변경이 아니라 제거이므로 allowshrinking 이 붙는 -keepclassmembernames 로는 부족하고,
# 제거까지 막는 -keepclassmembers 를 써야 한다.
#
# 대상을 enum 의 public static final 필드로 한정해 프로퍼티 백킹 필드
# (pngCode, gifCode 등) 는 계속 난독화되게 둔다.
# 클래스 이름 자체도 난독화 대상이다(정적 호출이라 리플렉션이 아니다).
#
# wear / mobile 두 모듈이 같은 FQCN 패키지를 쓰므로 한 줄로 양쪽이 덮인다.
# ---------------------------------------------------------------------------
-keepclassmembers enum com.monglife.mongs.presentation.view.assets.** {
    public static final <fields>;
}

# ---------------------------------------------------------------------------
# 크래시 스택트레이스 역추적
#
# 두 지시어가 모두 있어야 mapping.txt 로 retrace 할 수 있다.
# -keepattributes 만 넣고 -renamesourcefileattribute 를 빼면 원본 .kt 파일명이
# 그대로 노출되어 난독화 목적에 역행한다.
#
# -renamesourcefileattribute 는 AGP 9 부터 라이브러리 consumer rules 에서
# 금지된 전역 옵션이다(ConsumerRuleGlobalGuardian). 반드시 이 앱 전역 파일에 둬야 한다.
#
# 배포마다 app/*/build/outputs/mapping/release/mapping.txt 를 보관해야 의미가 있다.
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# 릴리스 로그 제거
#
# Log.w / Log.e 는 남긴다. BaseViewModel 의 Log.e 가 릴리스에서 미처리 예외를
# 확인할 수 있는 유일한 창구이고, MqttClient 의 Log.w 5곳이 연결 장애 진단 수단이다.
# 지우면 프로덕션 이슈가 전혀 보이지 않는다.
#
# 현재 Log.d / Log.v 호출부는 0곳이라 실효 대상은 Log.i 뿐이다.
# d / v 를 함께 넣는 것은 앞으로 추가될 디버그 로그가 릴리스로 새지 않게 하는 안전망이다.
#
# 이 지시어는 "호출을 지워도 된다" 는 뜻이라, 인자 계산에 부수효과가 없다고
# 증명될 때만 인자까지 함께 사라진다. 로그 인자를 만들며 파싱까지 하는 곳
# (MqttLogConsumer)은 이 룰로 지워지지 않으므로 코드에서 직접 게이트했다.
# ---------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
