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
