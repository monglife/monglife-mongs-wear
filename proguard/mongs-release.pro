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
