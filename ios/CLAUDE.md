# CLAUDE.md — iOS / watchOS

`../android` 의 Wear OS 앱을 watchOS 로 마이그레이션하는 프로젝트다.
Android 쪽 규약은 [`../android/CLAUDE.md`](../android/CLAUDE.md) 에 있고, 이 문서는
**그 구조가 여기서 어떻게 대응되는지**와 iOS 고유의 함정만 다룬다.

---

## 빌드 / 실행

`.xcodeproj` 는 `project.yml` 에서 생성된다. 커밋하지 않으므로 clone 직후엔 없다.

```bash
brew install xcodegen
cd ios
xcodegen generate
```

프로파일은 Android 의 flavor 와 1:1 대응한다 — `Local` / `Dev` / `Stg` / `Prd`.
스킴 이름에 프로파일이 붙는다.

```bash
# 패키지 단독 빌드/테스트 — 시뮬레이터 없이 호스트(macOS)에서 바로 돈다. 가장 빠른 피드백 루프.
swift build --package-path Packages/MongsKit
swift test  --package-path Packages/MongsKit

# 앱 빌드
xcodebuild -project MongsWear.xcodeproj -scheme MongsWear-Dev \
  -destination 'platform=watchOS Simulator,name=Apple Watch Series 11 (46mm)' build

# ⚠️ 릴리스도 따로 확인한다. 스킴 기본 설정이 Debug 라 위 명령은 최적화를 돌리지 않는다.
# 최적화에서만 터지는 컴파일러 크래시가 실제로 있었다 (아래 "함정 모음" 참고).
xcodebuild -project MongsWear.xcodeproj -scheme MongsWear-Prd -configuration Release-Prd \
  -destination 'generic/platform=watchOS Simulator' CODE_SIGNING_ALLOWED=NO build

# 시뮬레이터에 설치 후 실행
xcrun simctl list devices available | grep -i watch      # UDID 확인
xcrun simctl boot <UDID>
xcrun simctl install <UDID> <DerivedData>/Build/Products/Debug-Dev-watchsimulator/Mongs.app
xcrun simctl launch <UDID> com.mongs.wear
xcrun simctl io <UDID> screenshot demo.png
```

> Xcode 를 App Store 로 설치했고 `xcode-select` 가 아직 CommandLineTools 를 가리키면
> `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer` 로 전환한다.
> 그 전까지는 명령마다 `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer` 를 붙여도 된다.

---

## 구조 — 단순 MVVM

Android 는 헥사고날 25모듈이지만 **여기서는 미러링하지 않는다.**
Android 의 UseCase 50개는 대부분 어댑터 한 줄을 감싸는 껍데기라, Service 메서드가 그 자리를 대신한다.

```
View (SwiftUI, 앱 타겟)
  ↓
MongsViewModel      @Observable @MainActor — 화면당 하나
  ↓
MongsService        APIClient · HealthKit · Keychain · MQTT · 스프라이트 · 로컬 캐시
  ↓
MongsModel          도메인 모델 + 서버 DTO (의존성 0)
```

| 타겟 | 위치 | Android 대응 |
|---|---|---|
| `MongsModel` | `Packages/MongsKit/Sources/MongsModel` | `domain/*` + `application/*/port/web/response` |
| `MongsService` | `.../MongsService` | `core/*` + `data/*` |
| `MongsViewModel` | `.../MongsViewModel` | `presentation/viewmodel-presentation` |
| 앱 타겟 | `Apps/WearApp` | `app/wear-app` + `wear-view-presentation` |

### 지켜야 할 제약

- **`MongsModel` 의 `dependencies` 는 비어 있어야 한다.** 모델은 아무것도 모른다.
- `MongsViewModel` 은 Service 만 안다. HTTP·HealthKit·MQTT 는 모른다.
- 앱 타겟은 얇게 유지한다 — Android `app/wear-app` 이 239 LOC 뿐인 것과 같은 원칙.

### Kotlin → Swift 대응

| Android | 여기 |
|---|---|
| UseCase (`operator fun invoke()`) | Service 메서드 — 레이어를 따로 두지 않는다 |
| Hilt `@Module` / `@Inject` | `Apps/WearApp/Sources/DI/AppContainer.swift` (수동 DI) |
| `Flow<T>` | `AsyncStream<T>` |
| `StateFlow` + `collectAsState()` | `@Observable` 프로퍼티 (뷰가 읽기만 하면 자동 갱신) |
| `ViewModel` + `viewModelScope` | `@Observable @MainActor final class` + SwiftUI `.task {}` |
| `Vo.of(model)` companion factory | 모델의 computed property 로 흡수 |
| `ErrorCode.isMessageShow()` | `DeviceError.isMessageShown` |
| `LayoutView` 게이트 | `RootView.swift` |
| Coil `ImageLoader` + `LocalImageLoader` | `SpriteStore` (actor 캐시) + `SpriteLoader` (뷰 어댑터) |
| `MongResourceCode` (R.drawable int) | `MongsModel/MongResourceCode.swift` (번들 파일 이름) |

## 공통 기반

| 만든 것 | 위치 | Android 원본 |
|---|---|---|
| `APIClient` | `MongsService/APIClient.swift` | `core/data-core/global/Module.kt` (OkHttp ×2 + Retrofit ×2) |
| 401 재발급 | `MongsService/SessionRefresher.swift` | `web/interceptor/AuthorizationInterceptor.kt` |
| 세션/기기ID 보관 | `MongsService/TokenStore.swift` | `persistence/datastore/SessionDataStore.kt` + `crypto/SessionCipher.kt` |
| JSON 코덱 | `MongsModel/MongsCoding.swift` | `GsonLocalDateTimeFormatAdapter` / `GsonLocalTimeAdapter` |
| 응답 봉투 | `MongsModel/APIResponse.swift` | `web/dto/response/ResponseDto.kt` |
| 전역 오류 채널 | `MongsService/ErrorBanner.swift` | `BaseViewModel.errorEvent` (전역 싱글턴) |
| ViewModel 실행 헬퍼 | `MongsViewModel/ViewModelSupport.swift` | `BaseViewModel.viewModelScopeWithHandler` |
| Apple 로그인 | `MongsService/AppleSignInClient.swift` | `core/auth-core/.../GoogleAuthClient.kt` |
| 인증 서비스 | `MongsService/AuthService.swift` | `application/auth-application` + `AuthWebAdapter.kt` |

### 반드시 유지해야 하는 것들

Android 가 **버그를 겪고 도입한** 장치들이다. 구조를 단순화해도 이건 그대로 간다.

- **재발급 version 카운터** (`Session.version`). 401 이 동시에 여러 개 터지면 각 요청은
  자기가 보냈던 세션의 version 과 저장소의 현재 version 을 비교한다. 저장소가 더 크면
  남이 이미 갱신한 것이므로 refreshToken 을 다시 쓰지 않는다. 이게 없으면 refreshToken 이
  중복 소모되어 서버가 세션을 끊는다. 테스트: `APIClientTests.refreshIsSingleFlight`
- **재발급 요청에는 토큰을 붙이지 않는다.** 붙이면 그 요청도 401 → 재발급 → 무한 재귀다.
- **날짜 코덱의 비대칭.** 쓸 때 `yyyy-MM-dd'T'HH:mm:ss.SSS`, 읽을 때 ISO(밀리초 유무 모두).
  대칭으로 고치고 싶어지지만 서버가 양쪽을 다르게 취급하고 있을 수 있다.
- **401 과 403 의 구분.** 401 = 인증 실패(재발급 대상), 403 = 인가 실패(세션 종료).
- **로그아웃은 서버가 실패해도 로컬 세션을 지운다.**

### 서버에 필요한 작업 (백엔드 저장소)

Apple 로그인 엔드포인트 2개가 아직 없다. 경로 상수는
`MongsService/AuthService.swift` 의 `joinPath` / `loginPath` 하나만 고치면 된다.

- `POST public/auth/join/apple` — `{socialAccountId, identityToken, email?, name?}`
- `POST public/auth/login/apple` — `{socialAccountId, identityToken, deviceId, appPackageName, deviceName, buildVersion}` → `{accountId, accessToken, refreshToken}`
- `identityToken` 을 Apple JWKS(`appleid.apple.com/auth/keys`)로 검증하고 `sub` 가 `socialAccountId` 와 같은지 확인한다
- 미가입이면 기존 `DISCOVERY-ACCOUNT-101` 코드를 그대로 준다 (클라이언트가 join 후 재로그인한다)

**주의**: Apple 은 이메일·이름을 **최초 인증 때만** 준다. 서버가 join 시점에 저장해야 한다.
이메일이 `@privaterelay.appleid.com` (비공개 릴레이)일 수 있으니 유니크 제약을 확인할 것.

### 서버 없이 검증하기

`local` 프로파일은 `127.0.0.1:8010`(discovery) / `:8000`(gateway) 를 본다.
간단한 목 서버를 띄우면 앱 게이트 전환까지 실기기·실서버 없이 확인할 수 있다.

```bash
# discovery 만 흉내내는 목 서버 예시는 대화 기록 참고
xcodebuild ... -scheme MongsWear-Local build
xcrun simctl install <UDID> .../Debug-Local-watchsimulator/Mongs.app
```

## 환경 설정 (Android flavor 대응)

Android 는 `configs/core/data-core/<flavor>/values/config.xml` 을 플레이버별 `res.srcDir`
로 갈아끼운다. 여기서는 이렇게 흐른다:

```
Configurations/<Profile>.xcconfig  →  Apps/WearApp/Info.plist  →  MongsCore/AppConfig.swift
```

**⚠️ xcconfig 에서 `//` 는 주석이다.** `http://host` 를 그대로 쓰면 스킴 뒤가 통째로 잘린다.
빈 변수를 끼워 넣어 피한다:

```
MONGS_GATEWAY_API_URL = http:/$()/100.0.0.10:8000/api/
```

Xcode configuration 은 프로파일 × 빌드타입으로 8개다 (`Debug-Dev`, `Release-Prd`, …).
Android 의 flavor × buildType 과 같은 구조다.

### 비밀값

MQTT username/password 는 **커밋되는 xcconfig 에 넣지 않는다.** 이 저장소는 공개고,
Android 는 같은 값을 private 서브모듈 `configs`(= `monglife-mongs-wear-sub`)에 둔다.

여기서는 gitignore 된 `Configurations/Secrets.local.xcconfig` 로 뺐다.
`Base.xcconfig` 가 `#include?` 로 읽으므로 **파일이 없어도 빌드는 된다** —
값이 빈 문자열이 되고 `MQTTBroker.isConfigured` 가 false 라 MQTT 만 꺼진다.

```bash
cp Configurations/Secrets.local.xcconfig.example Configurations/Secrets.local.xcconfig
# 값을 채운 뒤 xcodegen generate
```

---

## 새 세로 슬라이스 추가 레시피

Android `CLAUDE.md` 의 슬라이스 레시피와 같은 순서다. 걸음 수 슬라이스가 그대로 예제다.

1. `MongsDomain/` — 순수 모델 (`Step.swift`, `StepExchangeRate.swift`)
2. `MongsApplication/` — Port 프로토콜 (`DevicePersistencePort.swift`)
3. `MongsApplication/` — VO (`StepVo.swift`, `static func of(_:)`)
4. `MongsApplication/` — UseCase 하나에 클래스 하나 (`ExchangeWalkingCountUseCase.swift`)
5. `MongsApplication/` — 에러 (`DeviceError.swift`)
6. `MongsData/` — Port 구현 actor (`SimulatedStepAdapter.swift`)
7. `MongsPresentation/` — `@Observable @MainActor` ViewModel (`MainStepViewModel.swift`)
8. `Apps/WearApp/Sources/` — SwiftUI 뷰 + `AppContainer` 에 조립 추가

---

## 펫 코어 루프

| 만든 것 | Android 원본 |
|---|---|
| `MongsModel/Mong.swift` | `domain/mong-domain/.../Mong.kt` |
| `MongsModel/MongContract.swift` | `ManagementResponseDto.kt` + 부분 병합 |
| `MongsService/MongService.swift` | `ManagementWebAdapter` + `ManagementPersistenceAdapter` |
| `MongsService/MongCache.swift` | Room `MONGS-MONG-DATABASE` |
| `MongsViewModel/MainSlotViewModel.swift` | `MainSlotViewModel.kt` |
| `Apps/WearApp/Sources/SlotContentView.swift` 외 | `SlotContent.kt` / `ConditionContent.kt` / `MainView.kt` |

### `Mong` 은 클래스가 아니라 값 타입이다

Android `Mong.kt` 는 `private set` 프로퍼티 21개 + 상태변경 메서드 9개를 가진 클래스지만,
**그 메서드들에 게임 로직이 하나도 없다** — 전부 서버가 계산한 값을 대입하는 setter 다.
실체는 "서버 응답의 일부 필드만 갱신하는 부분 병합"이라, Swift 에서는 값 타입 + `applying(_:)` 로 옮겼다.

**응답마다 실려 오는 필드가 다르다**는 점이 핵심이다. 쓰다듬기 응답은 경험치와 시각만 주므로
똥 개수·몸무게는 캐시 값을 그대로 둬야 한다. `MongTests` 가 이걸 고정한다.

### 로컬 캐시에 SwiftData 를 쓰지 않는다

캐시할 대상이 현재 몽 **하나**뿐이다. Room 이 필요했던 건 Android 가 여러 슬롯의 몽과 옵션을
관계로 들고 있었기 때문이고, 여기서는 actor + `Codable` 파일 저장으로 충분하다.

---

## 서버 연동 — 알아야 할 것들

로컬 discovery(8010) / gateway(8000) 에 실제로 붙여 확인한 내용이다.

### ⚠️ 로그인보다 기기 등록이 먼저다

```
POST public/userDevice   →   POST public/auth/login
```

등록되지 않은 `deviceId` 로 로그인하면 `DISCOVERY-DEVICE-101`(기기 정보가 존재하지 않습니다)
를 받는다. Android `LoginUseCase` 도 `createDevice` → `login` 순서다.
`AuthService.signIn()` 이 이 순서를 지킨다.

### ⚠️ `fcmToken` 이 `@NotBlank` 다

기기 등록 요청의 `fcmToken` 을 서버가 빈 값으로 받지 않는다. iOS 는 FCM 을 쓰지 않으므로
현재 `"ios-apns-pending"` 자리표시자를 보낸다. APNs 를 붙일 때 서버도 토큰 종류를
구분하도록 고쳐야 한다. (백엔드 핸드오프 문서에 기록됨)

### ⚠️ 앱 버전이 서버에 등록돼 있어야 한다

`GET public/auth/verify/version` 은 서버 DB 에 있는 (패키지, 버전) 조합만 통과시킨다.
없으면 `DISCOVERY-DEVICE-100` 400 이고, **로그인 자체가 막힌다.**

현재 로컬 서버에는 `com.mongs.wear` 가 `2.3.0` 까지만 있어서,
`Configurations/Local.xcconfig` 에서 `MONGS_BUILD_VERSION = 2.3.0` 으로 덮어쓰고 있다.
**서버에 iOS 버전 행이 추가되면 그 줄을 지운다.**

### 서버 시각 형식

- `sleepAt` / `wakeupAt` → `"22:00"` (초 없음)
- `createdAt` / `updatedAt` → `"2026-09-09T22:01:55.992"` (밀리초 있음)

둘 다 `MongsCoding` 의 읽기 형식 목록에 들어 있다. 형식을 늘릴 때 그 목록에 추가한다.

### 개발용 로그인 (Debug 전용)

서버에 Apple 엔드포인트가 나오기 전까지, **기존 legacy 로그인으로 진짜 세션**을 받아
화면 작업을 이어간다. 가짜 세션을 넣지 않으므로 gateway 호출·토큰 재발급까지 실제 경로를 탄다.

```bash
xcrun simctl launch <UDID> com.mongs.wear \
  -MongsDevLoginEmail ios-dev@monglife.test \
  -MongsDevLoginSocialId ios-dev-000001
```

Apple 엔드포인트가 나오면 `AuthService.devSignIn` 과 `RootViewModel.devLoginCredential` 을 지운다.

---

## 걸음 수집 (HealthKit)

Android 파이프라인 9파일 중 **4개가 통째로 사라졌다** —
`StepPassiveListenerService`, `StepBootReceiver`, `StepMaintenanceWorker`, WorkManager 설정.
`HKObserverQuery` + 백그라운드 전달이 그 역할을 전부 한다.

| 만든 것 | Android 원본 |
|---|---|
| `MongsModel/StepAccumulator.swift` | `domain/device-domain/.../StepAccumulator.kt` + `StepRestore.kt` |
| `MongsModel/StepWalletState.swift` | `DeviceDataStore.kt` 의 걸음 키들 |
| `MongsService/StepWalletStore.swift` | `DeviceDataStore.kt` + `StepCollector.kt` |
| `MongsService/HealthStepReader.swift` | `HealthServicesStepManager.kt` + `StepSensorManager.kt` |
| `MongsService/HealthKitStepService.swift` | `StepCollectionCoordinator.kt` + `DevicePersistenceAdapter.kt` |

### 크게 단순해진 것

Android 는 Health Services 가 **부팅 이후 경과 시간** 기준의 데이터포인트를 줘서 재부팅을
직접 감지해야 했다 — `StepCursor.bootMarkOf` 의 10초 양자화와 30초 허용오차,
일일 누계의 자정 창 처리, 센서 역행 감지가 전부 그것 때문이다.

HealthKit 의 `HKAnchoredObjectQuery` 는 **앵커가 커서를 대신한다.** 앵커 이후의 새 샘플만
돌려주므로 재전달·중복이 없고, 타임스탬프도 부팅 상대가 아니라 벽시계다.
그래서 `StepCursor` 전체가 `anchor: Data?` 한 필드로 접혔다.

### 그래도 반드시 지켜야 하는 것

Android 가 이중 카운트/유실을 겪고 넣은 장치들이다.

- **첫 관측은 기준선만 잡고 적립하지 않는다.** 앵커 없이 물으면 HealthKit 은 **가진 과거를
  전부** 준다. 그대로 적립하면 앱 설치 전 걸음이 통째로 입금된다.
- **`MAX_CREDIT_PER_BATCH = 100_000` 클램프.** 이상값 안전판.
- **source gate** (`credit(expectedSource:)`). 경로가 `none` 으로 내려간 뒤 도착한 알림을 버린다.
- **읽기-계산-쓰기가 한 호출 안에서 끝난다.** actor 가 그 트랜잭션이다.
- **복구(`restore`)는 적립 경로를 타지 않는다.** 이미 차감했던 걸음이라 source gate 를
  통과할 수 없고, 중복 판정과 잔액 반영이 갈라지면 MQTT 재전달 두 개가 동시에 통과한다.
- **경로가 바뀌면 앵커를 버린다.** 앵커는 경로마다 의미가 다르다.
- 삭제된 HealthKit 샘플(`deletedObjects`)은 **무시한다.** 이미 사용자가 써 버렸을 수 있는
  걸음을 되돌리면 잔액이 음수로 밀린다.

### ⚠️ iOS 는 읽기 권한 거부를 알려주지 않는다

`HKHealthStore.authorizationStatus(for:)` 는 **읽기** 타입에 대해 언제나 `.notDetermined` 를
돌려준다. "어떤 데이터를 안 준다"는 사실 자체가 정보 유출이라서다.

Android 는 `PermissionUtil.verifyActivityPermission()` 으로 직접 확인할 수 있었고
`MainStepViewModel.activityPermission` 을 화면에 그렸지만, **여기서는 그게 불가능하다.**
거부됐을 때 앱이 보는 것은 빈 결과뿐이고 그건 "아직 안 걸었다"와 구별되지 않는다.
그래서 권한 다이얼로그 대신 걸음이 0일 때 안내 문구만 남긴다.

### ⚠️ 환전은 로컬 차감 + 서버 통보 두 단계다

`StepService.exchange(units:)` 가 **로컬 지갑에서 먼저 깎고**, 그 다음
`POST user/step/exchange {mongId, walkingCount}` 로 서버에 알린다 (`StepExchangeClient`).

**둘째 단계가 빠지면 걸음만 사라지고 페이포인트를 못 받는다.** 실제로 `exchangeRemotely`
훅을 만들어만 두고 `AppContainer` 에서 연결하지 않아 그 상태로 한동안 있었다 —
화면은 멀쩡히 동작해서 서버 값을 확인하기 전까지 드러나지 않는다.

서버가 실패하면 **여기서 되돌리지 않는다.** 서버가 MQTT
(`{prefix}/device/{deviceId}/step/restore`)로 복구를 밀어 준다. 임의로 되돌리면 이중 적립이다.

### 검증

시뮬레이터에는 걸음 데이터가 없다. 회계 로직은 `HealthStepReader` 스텁으로 전부 덮여 있고
(`HealthKitStepServiceTests`), **실제 HealthKit 동작과 백그라운드 전달 빈도는 실기기에서만**
확인할 수 있다. UI 와 환전 왕복을 빠르게 보려면 **Debug 전용 실행 인자**로 시뮬레이션 구현으로 갈아끼운다:

```bash
xcrun simctl launch <UDID> com.mongs.wear -MongsSimulatedSteps YES
```

가짜 잔액을 넣는 게 아니라 구현을 통째로 바꾸는 것이라 **차감과 서버 통보가 실제와 같은
순서로 돈다** (`SimulatedStepService` 도 같은 `exchangeRemotely` 훅을 받는다).

## 배틀 (실시간 1:1)

Android `data/battle-data` + `pages/battle/*` (2,293 LOC) 이식.
**HTTP 와 MQTT 를 함께 쓰는 유일한 화면**이다.

| 만든 것 | Android 원본 |
|---|---|
| `MongsModel/Battle.swift` | `MatchEventDto` / `MatchQueueEventDto` / enum 3종 |
| `MongsService/BattleService.swift` | `data/battle-data/*` 어댑터 4개 |
| `MongsViewModel/BattleViewModel.swift` | `BattleMenuViewModel` + `BattleMatchViewModel` |
| `Apps/WearApp/Sources/BattleViews.swift` | `BattleMenuView` / `BattleMatchView` / 다이얼로그 |

### 토픽 7종

| 토픽 | 방향 |
|---|---|
| `battle/queue/{deviceId}` | 구독 — 매칭 성사 |
| `battle/match/{matchId}` | 구독 — 라운드 갱신 |
| `battle/match/over/{matchId}` | 구독 — 매치 종료 |
| `battle/queue/{mongId}` | 발행 — 대기열 이탈 |
| `battle/match/enter/{matchId}` | 발행 — 입장 |
| `battle/match/pick/{matchId}` | 발행 — 선택 |
| `battle/match/exit/{matchId}` | 발행 — 퇴장 |

⚠️ **대기열은 구독이 `deviceId`, 발행이 `mongId` 다.** 접두사가 같아 헷갈리기 쉽다.

`MQTTBroker` 는 `RealtimeService` 와 **같은 인스턴스를 공유한다** — 연결을 둘로 열지 않는다.

### 반드시 지켜야 하는 것들

- **구독을 먼저 걸고 등록/입장을 알린다.** 반대로 하면 바로 성사된 매칭이나
  즉시 시작된 라운드를 놓친다.
- **화면을 떠날 때 퇴장을 발행한다.** 안 보내면 상대가 끝까지 기다린다
  (원본도 `onCleared` 에서 보낸다). `.onDisappear` 에 걸어 뒀다.
- **최대 HP 는 첫 수신값으로 한 번만 고정한다.** HP 바의 분모라 매 라운드 갱신하면
  바가 줄지 않는다.
- 내가 누구인지는 **`deviceId` 로 가른다** — 서버가 따로 알려주지 않는다.

### ⚠️ 로컬에서 검증하지 못한 부분

`POST character/battle/queue/{mongId}` 가 **Redis 를 요구**하는데 로컬 서버에 안 떠 있어
500 이 온다. 매칭이 성사되지 않아 **매치 화면 전체가 미검증**이다.
(클라이언트는 오류 배너 → 대기 상태 복귀까지 설계대로 동작했다.)

검증하려면 서버에 Redis 를 띄우고 **기기 두 대**가 필요하다.

---

## 훈련 (미니게임 3종)

Android `presentation/.../training/*` (4,518 LOC) 이식.

| 만든 것 | Android 원본 |
|---|---|
| `MongsModel/RunnerEngine.swift` | `runner/engine/{RunnerEngine,Runner,RunnerPlayer,RunnerHurdle}.kt` |
| `MongsModel/BasketballEngine.swift` | `basketball/engine/{BasketballEngine,Basketball,Ball,Basket}.kt` |
| `MongsModel/Training.swift` | `TrainingType` + `Activity*Dto` + 가위바위보 판정 |
| `MongsService/TrainingService.swift` | `ActivityWebAdapter.kt` |
| `MongsViewModel/TrainingViewModel.swift` | `Training*ViewModel.kt` 5개의 공통 부분 |
| `Apps/WearApp/Sources/Training*.swift` | `Training*Content.kt` + 다이얼로그 |

### 엔진은 `MongsModel` 에 둔다

원본은 Presentation 레이어에 있지만 **화면도 서버도 모르는 순수 계산**이다.
무엇보다 **원본에 테스트가 하나도 없어서**, 시뮬레이터 없이 돌릴 수 있는 곳에 둬야 검증이 된다.
`RunnerEngineTests` / `BasketballEngineTests` / `RockPaperScissorsTests` 가 규칙을 고정한다.

### 이식하며 알게 된 것 (테스트로 고정)

- **한 틱은 16ms 다.** `1000 / 60` 이 정수 나눗셈이라 16.67 이 아니다 —
  게임 시계가 벽시계보다 4% 느리고 장애물 생성도 그만큼 늦다.
  Android 도 `1000L / FRAME` 이라 **동작은 일치한다.** 고치면 난이도가 달라진다.
- **농구는 끌어당긴 거리가 세기에 영향을 주지 않는다.** 방향 벡터를 정규화하지 않고
  큰 쪽 성분을 `ballSpeed` 로 고정한 뒤 나머지를 비례로 맞추기 때문이다.
  1픽셀을 끌든 300픽셀을 끌든 같은 궤적이다.
- **반발 계수가 1보다 크다** (`tension = 1.18`). 림에 튕기면 더 빨라진다 —
  물리적으로는 이상하지만 낮추면 공이 림에 붙어버린다.
- 충돌 판정은 원본이 SAT(분리축 정리)로 풀지만 **두 도형 모두 회전하지 않는 사각형**이라
  겹침 검사와 결과가 같다. 읽기 쉬운 쪽으로 바꿨고 테스트가 동치를 지킨다.

### 옮기지 않은 것

원본 enum 에는 축구(`TR003`)와 참참참(`TR004`)도 있지만 **화면이 `// TODO: 플레이 섹션` 스텁**이고
서버 목록에도 없다. `TrainingService.types()` 가 **화면이 없는 종류를 걸러낸다** —
서버가 나중에 켜도 앱이 빈 화면으로 들어가지 않는다.

### ⚠️ 게임 화면 위쪽 양 모서리는 비워 둔다

왼쪽엔 watchOS 의 닫기(X) 버튼이, 오른쪽엔 시계가 겹친다. Android 에는 둘 다 없어서
원본은 위쪽 모서리에 점수를 둔다 — 그대로 옮기면 점수가 X 뒤로 숨는다.

---

## 인앱 결제 (StoreKit 2)

Android `core/billing-core/.../GoogleBillingClient.kt` + `pages/charge/*` 이식.

| 만든 것 | Android 원본 |
|---|---|
| `MongsService/PurchaseClient.swift` | `GoogleBillingClient.kt` |
| `MongsService/StoreService.swift` | `StoreWebAdapter` + `GetProductsUseCase` |
| `MongsViewModel/ChargeViewModel.swift` | `ChargeStarPointViewModel.kt` |
| `Apps/WearApp/Sources/ChargeView.swift` | `ChargeStarPointView.kt` |
| `MongsModel/Store.swift` | `Product` / `Order` / `*Vo` / `StoreRequestDto` / `StoreResponseDto` |

### Google Play Billing → StoreKit 2 대응

| Android | 여기 |
|---|---|
| `queryProductDetails` | `Product.products(for:)` |
| `launchBillingFlow` + `PurchasesUpdatedListener` | `product.purchase()` — 결과가 그 자리에서 온다 |
| `queryPurchasesAsync` (미소비 주문 회수) | `Transaction.unfinished` |
| `consumeAsync` | `transaction.finish()` |
| `orderId` | `Transaction.id` |
| `purchaseToken` | 서명된 트랜잭션 **JWS** (`VerificationResult.jwsRepresentation`) |

**소모품은 `finish()` 하기 전까지 `Transaction.unfinished` 에 남는다.**
Android 가 `queryPurchasesAsync` 로 하던 회수 경로가 정확히 그 자리다.

### 반드시 유지해야 하는 것들

원본이 **중복 소비/미지급 사고를 겪고** 넣은 장치들이다. 결제는 되돌릴 수 없다.

- **서버 지급이 끝난 뒤에만 `finish()` 한다.** 먼저 닫으면 서버가 실패했을 때 스토어에도
  주문이 안 남아 **결제하고 못 받는** 상태가 된다.
- **소비 시도 기록은 요청 *전에* 남긴다** (`attemptedOrderIds`). 실패로 빠져나가도 자동
  재시도가 돌지 않게 — 사용자가 "소비" 버튼으로 직접 재시도한다.
- **자동 소비는 진입과 복귀에서만.** 오류 복구 훅에 넣으면 실패 → 복구 → 재시도 → 실패
  무한 루프가 된다.
- **결제 중에는 화면 전체를 덮어 터치를 막는다.** 버튼 하나만 막았다가, 재조회로 "소비"
  버튼이 로딩바 위로 드러나 중복 소비 요청이 나가는 사고가 있었다.
- **로딩과 내용이 배타가 아니다.** 결제 중에는 목록을 유지한 채 덮개만 씌운다.
- **미소비 주문이 있으면 "구매" 대신 "소비" 버튼**을 띄운다 — 자동 회수가 실패했을 때의 폴백.

### ⚠️ 가격은 서버가 아니라 스토어가 정한다

서버 `price` 는 원화 정수지만 사용자가 실제로 내는 금액과 통화는 App Store 가 정한다
(지역·세금·환율). 서버 값은 상품 식별·정렬에만 쓰고, 화면에는
`Product.displayPrice` 를 그린다. 스토어를 못 읽으면 서버 값으로 떨어진다.

### 유료 계정 없이 검증하기

`Configurations/Mongs.storekit` 이 App Store Connect 를 대신한다.
`MongsWear-Local` 스킴에 연결해 뒀다.

**⚠️ 스킴 설정이라 `simctl launch` 로는 적용되지 않는다.** 결제 흐름을 보려면
**Xcode 에서 그 스킴으로 실행**해야 한다. `simctl` 로 띄우면 상품을 못 찾아
표시가가 서버 값(`500원`)으로 떨어지고 구매 버튼은 오류가 난다.

---

## 위치 (맵 탐색)

Android `data/member-data/.../collection/web/manager/LocationSensorManager.kt` 대응.

원본은 `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)` 로
**한 번만** 읽는다 — 지속 추적이 아니다. 탐색 버튼을 누른 순간의 좌표만 필요하다.

`LocationClient` 가 `CLLocationUpdate.liveUpdates()` 로 같은 일을 한다.
스트림이지만 **첫 유효한 값을 받고 빠져나오면** 일회성 조회가 된다 — 델리게이트가 필요 없다.

### 알아 둘 것

- **`Info.plist` 에 `NSLocationWhenInUseUsageDescription` 이 없으면 권한 요청이 안 뜬다.**
  (HealthKit 은 아예 죽지만 위치는 조용히 실패한다 — 더 찾기 어렵다.)
- 권한 거부 플래그(`update.authorizationDenied`)는 **watchOS 11+** 에만 있다.
  10 에서는 스트림이 아무것도 주지 않으므로 15초 타임아웃이 대신 걸리고,
  빠져나온 뒤 `authorizationStatus` 로 사유를 가른다.
- 시뮬레이터 좌표는 `xcrun simctl location <UDID> set <lat>,<lng>` 로 넣는다.
  넣지 않으면 위치가 잡히지 않아 타임아웃까지 간다.
- **걸음(HealthKit)과 달리 위치는 상태를 읽을 수 있다.** 그래서 설정 화면에서
  활동 권한은 "다시 요청" 줄이고 위치 권한은 스위치다.

---

## 푸시 알림 (APNs)

Android `app/wear-app/.../service/NotificationService.kt` (FCM) 대응.

| 만든 것 | Android 원본 |
|---|---|
| `MongsService/PushService.swift` | `NotificationService.onNewToken` + `SyncUserDeviceUseCase` |
| `PushNotificationDelegate` (같은 파일) | `NotificationService.onMessageReceived` + `sendNotification` |
| `Apps/WearApp/Sources/DI/AppDelegate.swift` | FCM 서비스 등록 (`AndroidManifest`) |

Firebase 는 쓰지 않는다 — watchOS 지원이 제한적이고, standalone watch 앱은 APNs 를 직접 받을 수 있다.

### ⚠️ 알림 옵션 게이트가 클라이언트에 있을 수 없다

원본은 **FCM 데이터 메시지**를 받아 앱 코드가 알림을 만든다. 그래서 표시 직전에
`getNotificationOptionUseCase()` 를 확인하고 꺼져 있으면 안 띄운다.

APNs 의 `alert` 푸시는 **시스템이 먼저 표시**한다. 앱이 끼어들 자리가 없다.
`UNNotificationServiceExtension` 도 내용을 바꿀 수만 있고 억제하지는 못한다.
`content-available` 무음 푸시로 흉내낼 수는 있지만 watchOS 는 전달이 보장되지 않아
"알림이 가끔 안 온다"가 된다.

→ **옵션 판단이 서버로 올라가야 한다.** 서버 작업이 필요하고, 그때까지 설정의 알림 토글은
로컬 상태만 바꾼다. 백엔드 핸드오프 문서에 기록.

### 권한을 묻는 시점

**로그인 뒤 메인이 뜰 때** 묻는다 (`AppContainer.startPush`).
로그인 화면에서 묻지 않는 이유는 두 가지다 — 아직 계정이 없어 보낼 알림도 없고,
**iOS 는 한 번 거부하면 앱이 다시 물을 수 없다**(설정 앱에서만 바꾼다).

### 시뮬레이터로 검증한다

유료 계정도 실기기도 없이 표시·탭 경로를 다 볼 수 있다.

```bash
cat > push.json <<'JSON'
{ "Simulator Target Bundle": "com.mongs.wear",
  "aps": { "alert": { "title": "몽스", "body": "몽이가 배고파해요" }, "sound": "default" } }
JSON
xcrun simctl push <UDID> com.mongs.wear push.json
```

**권한을 먼저 허용해야 한다.** 거부 상태면 조용히 버려진다 —
`simctl push` 는 그래도 "Notification sent" 를 찍으므로 성공으로 착각하기 쉽다.
앱을 재설치하면 권한이 초기화된다.

실기기 토큰(`aps-environment`)은 유료 Developer Program 이 필요하다.

---

## MQTT 실시간 갱신

Android `core/data-core/.../mqtt/client/MqttClient.kt` (381 LOC) + 어댑터 3곳의 구독을 옮겼다.

| 만든 것 | Android 원본 |
|---|---|
| `MongsService/MQTTBroker.swift` | `MqttClient.kt` + `MqttConsumer` / `MqttRetryConsumer` |
| `MongsService/RealtimeService.swift` | `ManagementPersistenceAdapter` / `PlayerPersistenceAdapter` / `DevicePersistenceAdapter` 의 구독 부분 |
| `MongsModel/RealtimeEvent.swift` | `ManagementEventDto` / `PlayerEventDto` / `StepRestoreEventDto` |

라이브러리는 **`swift-server-community/mqtt-nio`** 다. CocoaMQTT 는 watchOS 를 지원하지 않는다.

### 토픽 4개

`{MongsMQTTTopic}` 접두사가 붙는다 (`mongs-dev` 등).

| 토픽 | 착지점 |
|---|---|
| `mong/management/{mongId}` | `MongService.apply(_:)` → 캐시 |
| `member/{accountId}/starPoint` | `PlayerService.apply(starPoint:)` |
| `member/{accountId}/slotCount` | `PlayerService.apply(slotCount:)` |
| `device/{deviceId}/step/restore` | `StepService.applyRestore(walkingCount:eventId:)` |

계정·기기 토픽은 로그인 뒤 `RealtimeService.start()` 가 한 번 연다.
몽 토픽만 `observeMong(_:)` 로 갈아끼운다 — `MainPagerView` 가 현재 몽 id 변화를 보고 부른다.

### 원본에서 그대로 가져온 것들

Android 가 중복 구독·유실 버그를 겪고 넣은 장치들이다.

- **구독 refcount + 공유 스트림.** 같은 토픽을 두 화면이 봐도 브로커 구독은 하나다
  (원본 `subscribeCounterMap` + `SharedFlowCache`).
- **정지 유예 5초.** 마지막 구독자가 사라져도 바로 UNSUBSCRIBE 하지 않는다.
  0 이면 화면을 옮길 때마다 구독 해제 → 재구독 왕복이 생긴다
  (`SharedFlowCache.DEFAULT_STOP_TIMEOUT_MILLIS`).
- **재연결하면 다시 SUBSCRIBE 한다.** `cleanSession = true` 라 브로커에 구독이 남지 않는다
  (원본 `MqttRetryConsumer.onConnectLost`).
- **QoS 2 / cleanSession true / keepAlive 180.** Android 연결 옵션과 같은 값.

### 알아 둘 것

- 유예 5초 때문에 **슬롯을 바꾼 직후 이전 몽의 이벤트가 도착할 수 있다.**
  `MongService.apply(_:)` 가 `mongId` 를 대조해 남의 상태를 버린다.
- `clientId` 는 매번 새로 만든다. 같은 값으로 두 번 붙으면 브로커가 앞의 연결을 끊는다.
- 페이로드는 HTTP 와 **같은 봉투**(`ResponseDto`)에 담겨 온다 — `APIResponse<T>` 를 그대로 쓴다.
- 평문 TCP 라 `Info.plist` 의 `NSExceptionDomains` 에 브로커 호스트도 넣어야 한다.
- **watchOS 는 백그라운드에서 연결이 끊긴다.** 다만 Android 도 `cleanSession = true` 에
  retained 메시지가 없어 백그라운드 수신은 원래 안 된다 — 동작 패리티는 유지된다.

---

## 설정 화면 — iOS 에서 반만 옮겨지는 것

원본 `SettingView` 는 알림·활동·위치 **세 권한의 부여 여부를 직접 읽어** 스위치에 그린다.
iOS 는 그게 절반만 된다.

| 원본 | 여기 |
|---|---|
| 알림 권한 | `UNUserNotificationCenter` 가 실제 상태를 준다 — 스위치 그대로 |
| 활동 권한 | HealthKit **읽기** 권한은 언제나 `.notDetermined` 다. 스위치를 그리면 거짓말이라 "다시 요청" 줄로 바꿨다 |
| 위치 권한 | `CLLocationManager.authorizationStatus` 가 실제 상태를 준다 — 스위치 그대로 |

알림 **옵션**(`DeviceOptionStore`)과 알림 **권한**은 다른 값이다. 권한이 있어도 사용자가
앱 안에서 끌 수 있다. 원본처럼 권한이 없으면 옵션 스위치를 잠근다.

로그아웃은 `RootViewModel.signOut()` 으로 올려 보낸다 — 게이트를 로그인 화면으로 되돌려야 하고,
가는 길에 `AppContainer.stopRealtime()` 으로 MQTT 구독도 끊는다.

---

## 화면 — Android 이식 현황

로그인 + 메인 5쪽 + 슬롯/환전/먹이/인벤토리/설정까지 원본 레이아웃 그대로 옮겼다.
에셋도 Android 원본을 그대로 쓴다.

| 화면 | Android 원본 |
|---|---|
| `LoginView` | `layout/LoginContent.kt` (로고 0.6 / 버튼 0.4 비중) |
| `MainPagerView` | `pages/main/MainView.kt` (좌우 HorizontalPager) |
| `StepContentView` | `pages/main/StepContent.kt` (0.2 / 0.5 / 0.3 비중) |
| `ConditionContentView` | `pages/main/ConditionContent.kt` (2×2 게이지 + 경험치 링) |
| `SlotContentView` | `pages/main/SlotContent.kt` |
| `InteractionContentView` | `pages/main/InteractionContent.kt` (2/3/2 배치) |
| `ConfigureContentView` | `pages/main/ConfigureContent.kt` (1/2/2 배치) |
| `SlotPickView` 외 | `pages/slot/SlotPickView.kt` + 다이얼로그 2개 |
| `ExchangeMenuView` / `ExchangeView` | `pages/exchange/ExchangeMenuView.kt` + `ExchangeStepView.kt` + `ExchangeStarPointView.kt` |
| `FeedMenuView` / `FeedView` | `pages/feed/FeedMenuView.kt` + `FeedFoodView.kt` + `FeedSnackView.kt` |
| `InventoryView` | `pages/inventory/InventoryView.kt` + `component/pages/inventory/InventoryItem.kt` |
| `SettingView` | `pages/setting/SettingView.kt` |
| `ChargeView` | `pages/charge/ChargeStarPointView.kt` |
| `NoticeView` 외 | `pages/notice/NoticeView.kt` + `NoticeDetailDialog.kt` |
| `FeedbackView` | `pages/feedback/FeedbackView.kt` + `CreateFeedbackDialog.kt` |
| `RandomDrawView` | `pages/randomDraw/RandomDrawView.kt` + 다이얼로그 2개 |
| `CollectionMenuView` / `CollectionGridView` | `pages/collection/*.kt` |
| `MapSearchView` | `pages/map/SearchMapView.kt` |
| `TrainingFlowView` 외 | `pages/training/*.kt` + 다이얼로그 2개 |
| `BattleMenuView` / `BattleMatchView` | `pages/battle/*.kt` + 다이얼로그 2개 |
| `NotReadyView` | `mobile-view-presentation/.../pages/common/NotReadyView.kt` |
| `Theme/MongsButton.swift` | `component/common/button/*.kt` |
| `Theme/MongsWidgets.swift` | `PayPointBox` · `ConditionSection` · `PageIndicator` · `LoadingBar` · `Logo` |

### 두 화면을 하나로 합친 것들

원본이 **경로와 필드명만 다르고 레이아웃이 완전히 같은** 화면을 둘씩 둔 자리가 있다.
`diff` 로 확인하고 하나로 합쳤다.

| 여기 | Android 원본 둘 | 가르는 값 |
|---|---|---|
| `FeedView` / `FeedViewModel` | `FeedFoodView` + `FeedSnackView` | `FeedItem.Kind` |
| `ExchangeView` / `ExchangeViewModel` | `ExchangeStepView` + `ExchangeStarPointView` | `ExchangeViewModel.Kind` |

**환전 쪽은 완전히 같지는 않다.** 합치면서 놓치기 쉬운 차이가 셋 있다:

- 버튼 색이 다르다 — 걸음은 `BlueButton`, 별가루는 `YellowButton`
- 가운데 위쪽 줄이 다르다 — 걸음은 글자만(`"N 걸음"`), 별가루는 아이콘 + `"x N"`
- 페이포인트 아이콘 크기가 다르다 (걸음 24, 별가루 26)

### ⚠️ 버튼 글자색은 스타일마다 다르다

`YellowButton` 은 `MongsDarkBrown`, `BlueButton` 은 **`MongsNavy`** 다.
한쪽 색으로 통일해 두면 파란 버튼이 전부 갈색 글자가 된다 (실제로 그렇게 새어 있었다).
`MongsButton.Style.titleColor` 가 그 구분을 들고 있다.

### ⚠️ 먹이 화면은 리스트가 아니라 캐러셀이다

`ScrollView` 로 목록을 그리고 싶어지지만 원본은 **한 번에 한 종류만** 보여주고
좌우 화살표(`SelectButton`)로 넘긴다. 아래 페이지 인디케이터의 점 개수가 곧 먹이 종류 수다.

### ⚠️ 인벤토리 서랍은 세 겹이다

`110×130` 짙은 회색(Compose `Color.LightGray` = `#CCCCCC`) 뒷판 위에
밝은 회색(`#F0F0F0`) 탭과 본체를 얹는다. 탭은 **왼쪽 상단**이다 —
원본 `Row` 가 `horizontalArrangement` 를 주지 않아 기본값이 Start 다.
탭을 가운데 두거나 뒷판을 빼면 서랍 손잡이 모양이 사라진다.

### `ScalingLazyColumn` → `List(.carousel)`

중앙에서 멀수록 작아지고 흐려지는 Wear 의 리스트는 watchOS `List` 에
`.listStyle(.carousel)` 하나로 대응된다. 크라운 스크롤과 위치 인디케이터도 따라온다.

단, `Toggle` 에 `allowsHitTesting(false)` 를 걸어 그리기만 시키면 **그 영역이 죽는다.**
원본은 Chip 전체와 Switch 양쪽이 같은 콜백을 받으므로,
행 레이블에 `.contentShape(Rectangle())` 을 줘서 줄 전체를 탭 영역으로 만든다.

### 원본이 둘로 나눠 둔 걸 합친 자리 (2)

- **오류 신고** — 원본은 목록 화면 + 작성 다이얼로그다. 목록에 담을 게 없어서
  (등록만 하고 조회 API 가 없다) **작성 화면 하나로 합쳤다.** 제목 → 내용 두 단계는 그대로다.

### ⚠️ 랜덤 뽑기 — 원본은 버튼 조건과 서버 요구가 어긋나 있다

화면은 `disable = payPoint 부족 && 티켓 없음` 이라 **둘 중 하나만 있으면 열린다.**
그런데 `POST character/interaction/randomDraw/{mongId}` 는 **티켓만** 받는다 —
없으면 `500-101-007 충분한 랜덤 뽑기 티켓이 없습니다`.
**Android 는 페이포인트가 있어도 티켓이 없으면 그냥 실패한다.**

페이포인트를 쓰는 쪽은 별도 API 인 `POST .../randomDraw/ticket/{mongId}` (한 장에 100P)다.

여기서는 화면이 약속한 대로 동작시킨다 — `RandomDrawViewModel.draw()` 가
**티켓이 없으면 먼저 한 장 사고** 뽑는다. 원본과 다른 유일한 지점이다.

기계 회전은 **대기 중 0°**, 뽑는 동안만 ±7° 로 흔들린다.
흔들림 각도를 그냥 토글하면 가만히 있을 때도 기울어 보인다.

### 아직 없는 화면은 자리표시자로 보낸다

메인의 상호작용·설정 쪽 버튼 9개는 v1 범위 밖이라 갈 화면이 없다.
그냥 두면 **눌러도 아무 반응이 없어 고장난 것처럼 보인다.**

Android 모바일 앱이 같은 이유로 `NotReadyView` 를 만들어 뒀고, 문구와 구성을 그대로 옮겼다
(제목 / "준비 중이에요" / "돌아가기" BlueButton).

| 자리표시자로 가는 버튼 | `NotReadyDestination` |
|---|---|
| 도감 · 맵 탐색 · 랜덤 뽑기 · 훈련 · 배틀 | `.collection` `.searchMap` `.randomDraw` `.training` `.battle` |
| 도움말 · 충전 · 공지사항 · 오류 신고 | `.help` `.charge` `.notice` `.feedback` |

제목 문구는 Android `mobile-view-presentation/.../layout/Router.kt` 의 라우트-제목 목록 그대로다.
**실제 화면을 이식하면 `NotReadyDestination` 에서 그 케이스만 지우고 버튼을 새 화면에 연결한다.**

잠금 조건(`isAlive` / `canPlay`)은 원본 그대로 두었다 — 죽었거나 자는 몽은 자리표시자에도 못 간다.

### 화면 크기 보정 — `.ms`

Android 원본은 원형 **192~227dp 한 종류**에 맞춰 `.dp` 리터럴 538개를 하드코딩했다.
중앙 상수 파일이 없어 그 값들을 그대로 옮겼고, **기준이 된 화면은 46mm(208×248pt)** 다.

watchOS 는 폭이 훨씬 넓게 갈린다:

| 기기 | pt | 배율 |
|---|---|---|
| SE 3 40mm | 162×197 | 0.78 |
| SE 3 44mm | 184×224 | 0.88 |
| Series 11 42mm | 187×223 | 0.90 |
| **Series 11 46mm (기준)** | **208×248** | **1.00** |
| Ultra 3 49mm | 211×257 | 1.01 |

40mm 에서는 원형 버튼 3개 줄(54×3 + 8×2 = 178)이 화면(162)을 넘어 **양쪽이 잘려 나갔고**,
컨디션 게이지 2×2 도 좌우가 잘렸다. `MongsMetrics` + `CGFloat.ms` 가 그걸 보정한다.

**세로는 보정하지 않는다.** 이미 `available * 0.2` 같은 비율로 잡아 뒀고,
세로까지 따로 곱하면 원본의 세로 비중이 어긋난다. 가로 비율 하나만 쓴다.

#### ⚠️ 두 번 곱하지 않는 규칙

**Android dp 리터럴에서 온 숫자는 SwiftUI 치수가 되는 지점에서 딱 한 번 보정한다.**

| 어디서 | 누가 보정하나 | 호출부는 |
|---|---|---|
| `MongsButton` / `MongsCircleButton` / `MongsCircleTextButton` 파라미터 | 컴포넌트 안 | 원본 dp 그대로 (`width: 70`) |
| `PayPointBox` · `StarPointBox` · `ConditionGauge` · `PageIndicator` · `SelectButton` · `LoadingBar` · `EdgeProgressRing` · `MongsLogo` | 컴포넌트 안 | 원본 dp 그대로 |
| `MongView(bodySize:)` | 컴포넌트 안 | 원본 dp 그대로 (`bodySize: 120`) |
| `mongsFont(_:)` | 안에서 | 원본 sp 그대로 (`mongsFont(14)`) |
| 화면 파일의 `frame` / `padding` / `spacing` / `offset` / `cornerRadius` | **호출부** | `.ms` 를 붙인다 |
| 화면 파일의 `static let` 치수 상수 | **정의부에서 한 번** | 쓰는 쪽은 그대로 |

두 번 붙이면 40mm 에서 0.61 배가 되어 눈에 띄게 작아진다. 새 화면을 추가할 때
공용 컴포넌트에 넘기는 값에 `.ms` 를 붙이고 싶어지는데, 그게 바로 이중 보정이다.

### 페이저 규칙 (Android `MainPagerViewModel` 상수)

- 몽 있음: 걸음 / 컨디션 / 슬롯 / 상호작용 / 설정 — 밝기 `[0.4, 0.4, 0.0, 0.4, 0.4]`
- 몽 없음: 컨디션을 뺀 4쪽 — 밝기 `[0.4, 0.0, 0.4, 0.4]`
- **시작은 언제나 슬롯 쪽**이고, 슬롯만 배경을 어둡게 하지 않는다

### ⚠️ 돌아오지 않는 `.task` 뒤에는 아무것도 붙이지 않는다

`MainSlotViewModel.observe()` 는 끝에 몽 스트림을 도는 `for await` 가 있어 **돌아오지 않는다.**
그 뒤에 이어 붙인 초기화는 영영 실행되지 않는데, **컴파일도 되고 경고도 없다.**

실제로 MQTT 시작(`startRealtime`)과 푸시 등록(`startPush`)을 거기 붙였다가 **둘 다 죽어 있었다.**
MQTT 는 별도 하네스로만 검증해서 앱에서 한 번도 안 돌고 있다는 걸 오래 몰랐다.

함께 시작해야 하는 것들은 **별도 `.task` 블록**에 둔다 — 나란히 돌고, 뷰가 사라질 때 같이 취소된다.

### ⚠️ 페이저에서 겪은 함정 3개

1. **쪽 수가 도중에 바뀌면 선택된 쪽이 어긋난다.** 몽 유무로 쪽 수가 4↔5 로 달라지므로,
   `hasLoaded` 전에는 `TabView` 를 아예 만들지 않는다. Android 도 `uiState.loadingBar` 를
   먼저 보고 그 다음에 페이저를 만든다.
2. **`TabView` 안에 조건부 자식(`if hasMong { ... }`)을 두면 태그 매핑이 흔들린다.**
   쪽 목록을 배열로 만들고 `ForEach` 로 돌려 인덱스가 어긋날 여지를 없앴다.
3. **로딩은 페이저가 시작한다.** 자식 뷰(`SlotContentView.task`)에 맡기면
   "로딩이 끝나야 자식이 만들어지는데 자식이 로딩을 시작하는" 순환이 된다.

또 `MainSlotViewModel.observe()` 는 `hasLoaded` 를 올리기 **전에** 몽을 채운다.
`for await` 루프는 아직 한 번도 돌지 않았으므로, 안 그러면 "로딩은 끝났는데 몽은 nil" 인
한 프레임이 생기고 페이저가 그걸로 쪽 수를 정해 버린다.

### 스프라이트는 요청하면 알아서 준비된다

`SpriteLoader.sprite(named:)` 는 없는 스프라이트를 요청받으면 **스스로 디코드를 시작**하고
끝나면 관찰을 통해 뷰가 다시 그려진다.

처음에는 `preload` 목록에 적은 것만 돌려주게 만들었는데, 목록에서 빠진 스프라이트가
**오류 없이 그 부분만 비어서** 그려지는 사고가 반복됐다 (원형 버튼의 반투명 배경
`btn_bg_circle` 을 빠뜨려 테두리와 아이콘만 뜨는 식). 화면을 고칠 때마다 목록을 챙겨야 하는
규칙은 지키기 어려워서 구조로 막았다.

`preload` 는 이제 **첫 프레임 깜빡임을 줄이는 최적화**이지 필수가 아니다.

### 앱 아이콘

Android 어댑티브 아이콘(`ic_launcher_{background,foreground}.png`)을 합성해 만들었다.

**어댑티브 아이콘은 108dp 캔버스 중 안쪽 72dp 만 보인다.** 두 장 모두 그 여백을 갖고 있어서
**둘 다 1.5배(108/72)로 그려야** 런처에서 보이던 그림이 된다.
전경만 키우면 배경에 검은 테두리가 남는다.

앱 아이콘에는 알파가 허용되지 않는다 — 합성 시 `CGImageAlphaInfo.noneSkipLast` 로 불투명하게 만든다.

### 에셋 크기

원본 PNG 는 화면 크기 대비 과하게 크다 (34dp 로 그리는 아이콘이 283KB, 474px).
`sips -Z` 로 버튼·아이콘은 128px, 펫 몸통은 256px 로 줄여 **41% 감축**했다
(4.8MB → 2.8MB). 에셋을 추가할 때도 같은 기준으로 줄인다.

---

## 함정 모음

- **Release 최적화에서 컴파일러가 죽는 자리가 있다** (Xcode 26.6, `SimplifyCFG` 패스).
  `MongResourceCode.expressionOffset` 은 케이스가 40개 가까운 switch 인데, 이게 `MongView`
  안으로 인라인되면서 만들어지는 CFG 를 옵티마이저가 감당하지 못했다.
  `@inline(never)` 로 막았고 `MongView` 의 레이어도 각각 프로퍼티로 분리해 뒀다.
  **Debug 는 최적화를 안 돌려 드러나지 않는다** — 그래서 릴리스 빌드를 따로 확인한다.
- **standalone watch 앱은 `Info.plist` 에 `WKWatchOnly = true` 가 필수다.** 없으면
  `WKCompanionAppBundleIdentifier` 를 요구하며 시뮬레이터 설치가 거부된다.
- **Swift 6 strict concurrency**: `@MainActor` 클래스의 `deinit` 은 nonisolated 라
  격리된 저장 프로퍼티를 건드릴 수 없다. `Task` 를 붙들어 두는 대신 `.task {}` 에
  구독을 걸어 뷰 생명주기에 맡긴다 (`MainStepViewModel.observeWalkingCount`).
- `AsyncStream` 은 `map` 하면 타입이 바뀐다. `AsyncStream<T>` 를 유지하려면 다시 감싸야 한다
  (`ObserveCurrentWalkingCountUseCase` 참고).
- actor 안에서 `AsyncStream { continuation in }` 빌더는 nonisolated 클로저라
  actor 상태를 못 만진다. `AsyncStream.makeStream()` 을 쓴다.
- 저장 프로퍼티 `step` 과 메서드 `step()` 은 같이 못 둔다 → Port 메서드는 `currentStep()`.
- `swift test` 를 시뮬레이터 없이 돌리려고 `Package.swift` 에 `.macOS(.v14)` 를 함께 선언해 뒀다.
  배포 대상은 watchOS 뿐이다.
- **GIF 를 번들에 넣을 땐 `project.yml` 에서 `type: folder`** 로 폴더 참조를 만들어야 한다.
  일반 그룹으로 넣으면 파일이 번들 루트에 평평하게 깔려
  `Bundle.url(forResource:withExtension:subdirectory:)` 가 못 찾는다.
- **SwiftUI 에는 GIF 뷰가 없다.** `AnimatedSpriteSource` 가 `CGImageSource` 로 프레임을 미리
  디코드하고, `AnimatedSprite` 가 `TimelineView(.animation)` 으로 시각→프레임을 계산해 그린다.
  프레임 인덱스를 `@State` 로 두면 매 프레임 뷰 그래프가 무효화되므로 **상태를 두지 않는다.**
- 픽셀아트라 `Image` 에 `.interpolation(.none)` 을 반드시 준다. 없으면 뭉개진다.
- GIF 프레임 딜레이가 0.011초 미만이면 0.1초로 올린다 — 오래된 GIF 의 관례라 그대로 두면
  원본보다 눈에 띄게 빨라진다.
- 원본 에셋에 **`.gif` / `.GIF` 대소문자가 섞여 있다**(26개가 대문자). `SpriteStore` 가 양쪽 다 시도한다.
- **Info.plist 에 `<$(VAR)/>` 로 불리언을 주입할 수 없다.** Xcode 26 빌드 시스템이 변수 치환 전에
  plist 를 파싱해서 XML 오류가 난다. 그래서 ATS 는 프로파일별로 가르지 못하고
  **평문을 쓰는 호스트만 예외로 여는** 방식으로 갔다 (Android 의 `network_security_config.xml` 대응).
  개발 서버 주소가 바뀌면 `Apps/WearApp/Info.plist` 의 `NSExceptionDomains` 도 함께 고쳐야 한다.
- **iOS 는 앱을 지워도 Keychain 이 남는다.** `deviceId` 에는 의도한 동작이지만(재설치해도 같은
  기기로 인식돼야 한다) 세션에는 아니다 — Android 는 DataStore 가 앱과 함께 지워져 재설치하면
  로그아웃 상태로 시작한다. `TokenStore` 가 UserDefaults 표식으로 재설치를 감지해 세션을 버린다.
- **폰트를 못 찾으면 SwiftUI 가 조용히 시스템 폰트로 떨어진다.** 화면은 멀쩡한데 분위기만
  달라지므로, 픽셀 폰트가 안 보이면 `UIAppFonts` 등록과 번들 포함 여부부터 확인한다.
- **`NSHealthShareUsageDescription` 이 없으면 권한 요청 순간 앱이 죽는다.** 쓰기는 하지 않으므로
  `NSHealthUpdateUsageDescription` 은 넣지 않는다.
- **Sign in with Apple 은 유료 Developer Program 이 있어야 실기기에서 된다.**
  `MongsWear.entitlements` 의 capability 가 프로비저닝 프로파일에 없으면 설치가 거부된다.
  시뮬레이터는 영향받지 않는다.

---

## 제품 결정 기록

| 항목 | 결정 | 이유 |
|---|---|---|
| 화면 항상 켜기 | **기본 자동 디밍 수용** — `WKExtendedRuntimeSession` 을 쓰지 않는다 | Android 는 `FLAG_KEEP_SCREEN_ON` 이지만 코어 루프(걸음·먹이·환전)는 짧은 상호작용이라 영향이 적다. watchOS 는 세션 종류를 심사에서 검증하고 게임용 허용 카테고리가 제한적이며, 배터리 소모와 세션 만료·중단 처리 코드가 따라온다. **훈련/배틀(Phase 12~13)이 들어올 때 다시 판단한다.** |
| 실기기 검증 | **Phase 7 에서 빼고 별도 트랙** | Apple Watch 와 유료 Developer Program 이 둘 다 없다. 기기·계정이 준비되면 HealthKit 실제 걸음 / MQTT 백그라운드 / Apple 로그인을 한 번에 검증한다. |

---

## 마이그레이션 백로그

### 다음에 할 일

기능 이식 전에 **스파이크가 먼저 필요한 것들**이 있다. watchOS 는 지원 라이브러리가
Wear OS 만큼 넉넉하지 않아서, 라이브러리 선택이 아키텍처를 바꿀 수 있다.

| Android | watchOS | 상태 |
|---|---|---|
| Paho MQTT | `swift-server-community/mqtt-nio` | ✅ 완료 — 위 MQTT 절 참고 (CocoaMQTT 는 watchOS 미지원) |
| FCM | Firebase iOS SDK 또는 APNs 직접 | ⚠️ 스파이크 필요 — Firebase 의 watchOS 지원이 제한적 |
| Google 로그인 (Legacy / Credential Manager) | GoogleSignIn-iOS 또는 `ASWebAuthenticationSession` | ⚠️ 스파이크 필요 — watchOS 지원 확인 |
| Health Services Passive Monitoring | HealthKit `HKObserverQuery` + `enableBackgroundDelivery` | watchOS 엔 `PassiveListenerService` 대응물이 없다 |
| WorkManager 15분 주기 | `WKApplicationRefreshBackgroundTask` | |
| Room | SwiftData | 캐시 용도(테이블 2개)라 충분 |
| DataStore + Keystore AES-GCM | UserDefaults + Keychain (`kSecAttrAccessibleAfterFirstUnlock`) | |
| Retrofit ×2 + `AuthorizationInterceptor` | `URLSession` + async/await + 재발급 미들웨어 | 401 시 mutex + version 카운터로 중복 refresh 막는 로직 그대로 이식 |
| Play Billing | StoreKit 2 | ✅ 완료 — 위 인앱 결제 절 참고 |
| Wear Compose 14k LOC + 스프라이트 408개 | SwiftUI 재구현 | ✅ 화면 이식 완료 (도움말만 자리표시자) |

### 서버 API

Android 와 동일한 엔드포인트를 그대로 쓴다. 목록은 `../android/CLAUDE.md` 와
`core/data-core/.../web/client/` 참고. 두 개의 베이스 URL 이 있다:

- **discovery** (`MongsDiscoveryAPIURL`) — 인증. 토큰 없이 호출한다.
- **gateway** (`MongsGatewayAPIURL`) — 그 외 전부. `Authorization: Bearer` 필요.

### 아직 없는 것

`RootView.swift` 의 `TODO` 가 Android `LayoutView` 게이트의 남은 부분이다 —
강제 업데이트 체크(`GET public/auth/verify/version`), 로그인, 라우터.
