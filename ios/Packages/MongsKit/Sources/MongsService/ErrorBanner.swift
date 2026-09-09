import Foundation
import MongsModel

/// 전역 오류 채널
///
/// Android `core/presentation-core/.../viewmodel/BaseViewModel.kt` 의
/// `companion object { errorEvent }` 대응 — **프로세스 전역 싱글턴**이다.
/// 모든 ViewModel 이 여기로 오류를 밀어 넣고, 루트 뷰 하나가 구독해 배너를 띄운다.
/// DI 로 내려보내지 않는 성질을 그대로 유지한다.
///
/// watchOS 에는 Toast 가 없으므로 소비 쪽은 오버레이 배너로 그린다.
///
/// Android 는 `MutableSharedFlow()` 기본값(replay 0, buffer 0)이라 구독자가 없으면
/// `emit` 이 **정지**하고 메시지는 버려진다. 여기서는 버퍼를 조금 두되 넘치면
/// 오래된 것을 버린다 — 오류 배너는 최신 것이 중요하고, 밀어 넣는 쪽이 멈추면 안 된다.
public actor ErrorBanner {

    public static let shared = ErrorBanner()

    private var continuations: [UUID: AsyncStream<String>.Continuation] = [:]

    private init() {}

    public func messages() -> AsyncStream<String> {
        let (stream, continuation) = AsyncStream<String>.makeStream(
            bufferingPolicy: .bufferingNewest(4)
        )
        let id = UUID()
        continuations[id] = continuation

        continuation.onTermination = { [weak self] _ in
            Task { await self?.remove(id) }
        }
        return stream
    }

    public func post(_ message: String) {
        for continuation in continuations.values {
            continuation.yield(message)
        }
    }

    /// 오류가 사용자에게 보여줄 종류일 때만 배너에 올린다.
    /// Android `ErrorCode.isMessageShow()` 분기 대응.
    public func post(error: any Error) {
        if let apiError = error as? APIError {
            guard apiError.isMessageShown else { return }
            post(apiError.message)
        } else if let deviceError = error as? DeviceError {
            guard deviceError.isMessageShown else { return }
            post(deviceError.message)
        } else {
            post("알 수 없는 오류가 발생했습니다.")
        }
    }

    private func remove(_ id: UUID) {
        continuations[id] = nil
    }
}
