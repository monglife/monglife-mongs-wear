#if canImport(CoreLocation)
import CoreLocation
import Foundation

/// 위치 한 번 읽기
///
/// Android `data/member-data/.../collection/web/manager/LocationSensorManager.kt` 이식.
/// 원본은 `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)` 로
/// **한 번만** 읽는다 — 지속 추적이 아니다. 맵 탐색 버튼을 누른 순간의 좌표만 필요하다.
///
/// watchOS 는 `CLLocationUpdate.liveUpdates()` 로 같은 일을 한다. 스트림이지만
/// **첫 유효한 값을 받고 바로 빠져나오면** 일회성 조회가 된다. 델리게이트를 만들 필요가 없다.
public actor LocationClient {

    /// 위치를 못 잡을 때 무한정 기다리지 않는다. 시계에서 GPS 는 느릴 수 있다.
    private static let timeout: Duration = .seconds(15)

    public init() {}

    public enum LocationError: Error, LocalizedError {
        case denied
        case unavailable

        public var errorDescription: String? {
            switch self {
            case .denied: "위치 권한이 필요합니다"
            case .unavailable: "위치를 확인할 수 없습니다"
            }
        }
    }

    /// 권한 상태. 걸음(HealthKit)과 달리 **위치는 iOS 도 상태를 알려준다.**
    public nonisolated var isAuthorized: Bool {
        switch CLLocationManager().authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways: true
        default: false
        }
    }

    public nonisolated var isDenied: Bool {
        CLLocationManager().authorizationStatus == .denied
    }

    /// 현재 좌표를 한 번 읽는다.
    ///
    /// 권한을 아직 안 물었으면 `liveUpdates()` 가 시스템 다이얼로그를 띄운다.
    public func current() async throws -> (latitude: Double, longitude: Double) {
        if isDenied { throw LocationError.denied }

        let result: (Double, Double)? = await withTaskGroup(of: (Double, Double)?.self) { group in
            group.addTask {
                do {
                    for try await update in CLLocationUpdate.liveUpdates(.default) {
                        // 권한이 없으면 좌표 대신 플래그가 온다.
                        //
                        // 이 플래그는 watchOS 11+ 에만 있다. 10 에서는 거부 시 스트림이
                        // 그냥 아무것도 주지 않으므로 아래 타임아웃이 대신 걸린다
                        // (느릴 뿐 결과는 같다 — 빠져나온 뒤 `isDenied` 로 사유를 가른다).
                        if #available(watchOS 11.0, macOS 15.0, *),
                           update.authorizationDenied || update.authorizationDeniedGlobally {
                            return nil
                        }
                        if let location = update.location {
                            return (location.coordinate.latitude, location.coordinate.longitude)
                        }
                    }
                } catch {
                    return nil
                }
                return nil
            }
            group.addTask {
                try? await Task.sleep(for: Self.timeout)
                return nil
            }
            let first = await group.next() ?? nil
            group.cancelAll()
            return first
        }

        guard let result else {
            throw isDenied ? LocationError.denied : LocationError.unavailable
        }
        return (result.0, result.1)
    }
}
#endif
