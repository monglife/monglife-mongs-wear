#if canImport(HealthKit)
import Foundation
import HealthKit
import MongsModel

/// `HealthStepReader` 의 HealthKit 구현
public final class HealthKitStepReader: HealthStepReader, @unchecked Sendable {

    private let store = HKHealthStore()
    private let stepType = HKQuantityType(.stepCount)

    public init() {}

    public func isAvailable() -> Bool {
        HKHealthStore.isHealthDataAvailable()
    }

    /// 읽기 권한을 요청한다.
    ///
    /// ⚠️ **성공했다고 해서 권한을 받은 것이 아니다.**
    /// Apple 은 읽기 권한의 허용 여부를 앱에 알려주지 않는다 —
    /// `authorizationStatus(for:)` 는 읽기 타입에 대해 언제나 `.notDetermined` 를 준다.
    /// "어떤 데이터를 안 준다"는 사실 자체가 정보 유출이기 때문이다.
    ///
    /// 그래서 Android 처럼 "권한 있음/없음"을 화면에 그릴 수 없다.
    /// 거부됐을 때 앱이 보는 것은 **빈 결과**뿐이고, 그건 "아직 안 걸었다"와 구별되지 않는다.
    /// (Android 는 `PermissionUtil.verifyActivityPermission()` 으로 직접 확인할 수 있었다.)
    ///
    /// 여기서 던지는 것은 권한 거부가 아니라 요청 자체가 실패한 경우뿐이다.
    public func requestAuthorization() async throws {
        guard isAvailable() else { throw HealthStepError.unavailable }
        // 쓰기는 하지 않는다. 걸음은 시계가 기록하고 우리는 읽기만 한다.
        try await store.requestAuthorization(toShare: [], read: [stepType])
    }

    public func readSteps(since anchor: Data?) async throws -> (counts: [Int], anchor: Data?) {
        guard isAvailable() else { throw HealthStepError.unavailable }

        return try await withCheckedThrowingContinuation { continuation in
            let query = HKAnchoredObjectQuery(
                type: stepType,
                predicate: nil,
                anchor: Self.decodeAnchor(anchor),
                limit: HKObjectQueryNoLimit
            ) { _, samples, _, newAnchor, error in
                if let error {
                    continuation.resume(throwing: HealthStepError.query(error.localizedDescription))
                    return
                }

                // 삭제된 샘플(deletedObjects)은 무시한다.
                // 이미 적립해 사용자가 써 버렸을 수 있는 걸음을 되돌리면 잔액이 음수로 밀린다.
                let counts = (samples as? [HKQuantitySample] ?? []).map {
                    Int($0.quantity.doubleValue(for: .count()))
                }

                continuation.resume(returning: (counts, Self.encodeAnchor(newAnchor)))
            }
            store.execute(query)
        }
    }

    public func observeUpdates() -> AsyncStream<Void> {
        let (stream, continuation) = AsyncStream<Void>.makeStream()

        let query = HKObserverQuery(sampleType: stepType, predicate: nil) { _, completion, error in
            if error == nil { continuation.yield(()) }
            // completionHandler 를 반드시 불러야 한다. 안 부르면 HealthKit 이
            // 백그라운드 전달을 중단한다.
            completion()
        }

        store.execute(query)
        // 앱이 잠들어 있어도 걸음이 쌓이면 깨워 달라고 등록한다.
        // watchOS 는 빈도를 보장하지 않는다 — 정확한 시점이 아니라 "언젠가"다.
        store.enableBackgroundDelivery(for: stepType, frequency: .hourly) { _, _ in }

        continuation.onTermination = { [store] _ in
            store.stop(query)
        }
        return stream
    }

    // MARK: - 앵커 직렬화

    private static func encodeAnchor(_ anchor: HKQueryAnchor?) -> Data? {
        guard let anchor else { return nil }
        return try? NSKeyedArchiver.archivedData(withRootObject: anchor, requiringSecureCoding: true)
    }

    private static func decodeAnchor(_ data: Data?) -> HKQueryAnchor? {
        guard let data else { return nil }
        return try? NSKeyedUnarchiver.unarchivedObject(ofClass: HKQueryAnchor.self, from: data)
    }
}
#endif
