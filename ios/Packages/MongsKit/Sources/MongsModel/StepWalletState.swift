import Foundation

/// 지갑 상태 (영속 대상)
///
/// Android `data/device-data/.../persistence/datastore/DeviceDataStore.kt` 의
/// 걸음 관련 키들을 하나의 값 타입으로 묶은 것.
///
/// **걸음 수는 기기 로컬에만 존재한다.** 서버는 잔액을 보관하지 않으므로
/// 앱을 지우면 미환전 잔액도 함께 사라진다.
public struct StepWalletState: Sendable, Equatable, Codable {

    /// 스키마 버전. 저장 구조가 바뀌면 올리고 마이그레이션한다.
    public static let currentSchemaVersion = 1

    public var schemaVersion: Int
    /// 확정 잔액
    public var balance: Int
    /// 현재 수집 경로. 정확히 하나만 활성이다.
    public var source: StepSource
    /// HealthKit 앵커. `nil` 이면 아직 기준선을 잡지 않았다.
    public var anchor: Data?
    /// 이미 반영한 복구 알림 식별자 (오래된 것부터)
    public var appliedRestoreEventIds: [String]

    public init(
        schemaVersion: Int = StepWalletState.currentSchemaVersion,
        balance: Int = 0,
        source: StepSource = .unresolved,
        anchor: Data? = nil,
        appliedRestoreEventIds: [String] = []
    ) {
        self.schemaVersion = schemaVersion
        self.balance = balance
        self.source = source
        self.anchor = anchor
        self.appliedRestoreEventIds = appliedRestoreEventIds
    }

    /// 화면에 그릴 값으로 바꾼다.
    ///
    /// `available` 은 수집 경로가 실제로 도는지다. false 면 UI 가 숫자 대신 "-" 를 그린다.
    /// HealthKit 은 배치로 내려주므로 걷는 중에는 지갑 반영이 늦는데,
    /// `pending` 이 그 사이 화면 숫자가 멈춰 보이지 않게 한다.
    public func step(pending: Int = 0) -> Step {
        Step(
            walkingCount: balance,
            pendingWalkingCount: pending,
            available: source.isCollecting
        )
    }
}
