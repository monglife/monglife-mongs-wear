import Foundation
import MongsModel
import MongsService

/// ViewModel 공통 실행 헬퍼
///
/// Android `BaseViewModel.viewModelScopeWithHandler` 대응.
/// 원본은 `CoroutineExceptionHandler` 를 스코프에 붙여 모든 코루틴의 예외를 한곳에서
/// 받아 토스트로 보내고, **500ms 를 기다린 뒤** 화면 복구 훅을 부른다.
/// 그 지연은 토스트가 보일 시간을 벌기 위한 것이다 — 바로 화면을 바꾸면 메시지가
/// 함께 사라져 버린다.
///
/// Swift 에서는 상속 대신 프로토콜 확장으로 준다. `@MainActor` 클래스가 채택하면
/// `run { }` 한 줄로 같은 동작을 얻는다.
@MainActor
public protocol ErrorReportingViewModel: AnyObject {
    /// 오류를 배너로 보낸 뒤 화면을 되돌릴 기회. 기본 구현은 아무것도 하지 않는다.
    func recoverFromError(_ error: any Error) async
}

extension ErrorReportingViewModel {

    public func recoverFromError(_ error: any Error) async {}

    /// 본문을 실행하고, 실패하면 전역 배너로 보낸 뒤 복구 훅을 부른다.
    ///
    /// 취소(`CancellationError`)는 오류가 아니다 — 화면을 떠날 때마다 배너가 뜨면 안 된다.
    @discardableResult
    public func run<T>(_ body: () async throws -> T) async -> T? {
        do {
            return try await body()
        } catch is CancellationError {
            return nil
        } catch {
            await ErrorBanner.shared.post(error: error)
            // Android 의 NAVIGATE_DELAY. 배너가 보인 뒤에 화면을 되돌린다.
            try? await Task.sleep(for: .milliseconds(500))
            await recoverFromError(error)
            return nil
        }
    }
}
