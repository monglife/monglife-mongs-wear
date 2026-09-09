// swift-tools-version: 6.0
import PackageDescription

/// 단순 MVVM 3계층.
///
/// ```
/// View (SwiftUI, 앱 타겟)
///   ↓
/// MongsViewModel   @Observable @MainActor — 화면당 하나
///   ↓
/// MongsService     APIClient · HealthKit · Keychain · MQTT · 로컬 캐시
///   ↓
/// MongsModel       도메인 모델 + 서버 DTO (의존성 0)
/// ```
///
/// Android 는 헥사고날 25모듈이지만 여기서는 미러링하지 않는다. Android 의 UseCase 50개는
/// 대부분 어댑터 한 줄을 감싸는 껍데기라, Service 메서드가 그 자리를 대신한다.
let package = Package(
    name: "MongsKit",
    platforms: [
        .watchOS(.v10),
        // 실제 배포 대상은 watchOS 뿐이다. macOS 를 함께 선언하는 이유는
        // `swift test` 를 시뮬레이터 없이 호스트에서 바로 돌리기 위해서다.
        .macOS(.v14),
    ],
    products: [
        .library(name: "MongsModel", targets: ["MongsModel"]),
        .library(name: "MongsService", targets: ["MongsService"]),
        .library(name: "MongsViewModel", targets: ["MongsViewModel"]),
    ],
    targets: [
        // ⚠️ dependencies 는 비어 있어야 한다. 모델은 아무것도 모른다.
        .target(name: "MongsModel"),

        .target(name: "MongsService", dependencies: ["MongsModel"]),

        // 뷰모델은 Service 를 직접 부른다. 그 아래(HTTP/HealthKit/MQTT)는 모른다.
        .target(name: "MongsViewModel", dependencies: ["MongsModel", "MongsService"]),

        .testTarget(name: "MongsModelTests", dependencies: ["MongsModel"]),
        .testTarget(name: "MongsServiceTests", dependencies: ["MongsService", "MongsModel"]),
    ],
    swiftLanguageModes: [.v6]
)
