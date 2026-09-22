import Foundation
import MongsModel
import Testing
@testable import MongsService

/// HealthKit 스텁
///
/// 실제 HealthKit 은 시뮬레이터에서도 권한 시트를 띄우고 실데이터가 필요해서,
/// 회계 흐름을 검증하려면 이음매가 필요하다.
private final class StubReader: HealthStepReader, @unchecked Sendable {

    private let lock = NSLock()
    private var batches: [[Int]]
    private var readIndex = 0
    private let updates: AsyncStream<Void>.Continuation?
    private let stream: AsyncStream<Void>

    let available: Bool
    let authorizationError: (any Error)?
    private(set) var authorizationRequested = false

    init(batches: [[Int]] = [], available: Bool = true, authorizationError: (any Error)? = nil) {
        self.batches = batches
        self.available = available
        self.authorizationError = authorizationError
        let (stream, continuation) = AsyncStream<Void>.makeStream()
        self.stream = stream
        self.updates = continuation
    }

    func isAvailable() -> Bool { available }

    func requestAuthorization() async throws {
        lock.withLock { authorizationRequested = true }
        if let authorizationError { throw authorizationError }
    }

    func readSteps(since anchor: Data?) async throws -> (counts: [Int], anchor: Data?) {
        lock.withLock {
            let counts = readIndex < batches.count ? batches[readIndex] : []
            readIndex += 1
            // 매 조회마다 새 앵커를 준다 — 실제 HealthKit 동작과 같다.
            return (counts, Data("anchor-\(readIndex)".utf8))
        }
    }

    func observeUpdates() -> AsyncStream<Void> { stream }

    /// HealthKit 이 새 데이터를 알린 것처럼 만든다.
    func emitUpdate() { updates?.yield(()) }
}

@Suite("HealthKit 걸음 서비스")
struct HealthKitStepServiceTests {

    private func makeService(reader: StubReader) -> (HealthKitStepService, StepWalletStore) {
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        return (HealthKitStepService(wallet: wallet, reader: reader), wallet)
    }

    @Test("수집을 시작하면 권한을 요청하고 경로가 잡힌다")
    func startsCollection() async {
        let reader = StubReader(batches: [[5_000]])
        let (service, wallet) = makeService(reader: reader)

        await service.startCollection()

        #expect(reader.authorizationRequested)
        #expect(await wallet.current().source == .healthKitAnchored)
    }

    @Test("첫 동기화는 적립하지 않고 기준선만 잡는다")
    func firstSyncIsBaseline() async {
        // HealthKit 이 과거 전체를 주기 때문에 그대로 적립하면 설치 전 걸음이 들어온다.
        let reader = StubReader(batches: [[9_999]])
        let (service, wallet) = makeService(reader: reader)

        await service.startCollection()

        #expect(await wallet.current().balance == 0)
        #expect(await wallet.current().anchor != nil)
    }

    @Test("기준선 이후의 갱신은 적립된다")
    func creditsAfterBaseline() async {
        let reader = StubReader(batches: [[9_999], [120], [80]])
        let (service, _) = makeService(reader: reader)
        await service.startCollection()   // 기준선

        reader.emitUpdate()
        reader.emitUpdate()

        // 관측 Task 가 처리할 때까지 스트림으로 기다린다
        var iterator = await service.stepStream().makeAsyncIterator()
        _ = await iterator.next()
        await waitUntil { await service.currentStep().walkingCount == 200 }

        #expect(await service.currentStep().walkingCount == 200)
    }

    @Test("HealthKit 을 쓸 수 없으면 경로가 none 이 된다")
    func unavailableLeadsToNoSource() async {
        let reader = StubReader(available: false)
        let (service, wallet) = makeService(reader: reader)

        await service.startCollection()

        #expect(await wallet.current().source == .none)
        #expect(!(await service.currentStep().available))
        #expect(!reader.authorizationRequested)
    }

    @Test("권한을 거부하면 경로가 none 이 된다")
    func deniedAuthorizationLeadsToNoSource() async {
        let reader = StubReader(authorizationError: HealthStepError.authorizationDenied)
        let (service, wallet) = makeService(reader: reader)

        await service.startCollection()

        #expect(await wallet.current().source == .none)
    }

    @Test("수집 시작은 멱등이다")
    func startIsIdempotent() async {
        let reader = StubReader(batches: [[1], [50], [50]])
        let (service, _) = makeService(reader: reader)

        await service.startCollection()
        await service.startCollection()
        await service.startCollection()

        // 두 번째·세 번째 호출이 동기화를 다시 돌렸다면 잔액이 올라갔을 것이다
        #expect(await service.currentStep().walkingCount == 0)
    }

    @Test("환전하면 잔액이 줄고 payPoint 를 돌려준다")
    func exchangeConsumes() async throws {
        let reader = StubReader(batches: [[1], [2_450]])
        let (service, _) = makeService(reader: reader)
        await service.startCollection()
        reader.emitUpdate()
        await waitUntil { await service.currentStep().walkingCount == 2_450 }

        let result = try await service.exchange(units: 2)

        #expect(result.payPoint == 200)
        #expect(result.step.walkingCount == 450)
    }

    @Test("잔액이 모자라면 환전이 실패하고 잔액은 그대로다")
    func exchangeRejectsOverdraft() async {
        let reader = StubReader(batches: [[1]])
        let (service, _) = makeService(reader: reader)
        await service.startCollection()

        await #expect(throws: DeviceError.notEnoughWalkingCount(requested: 1_000, balance: 0)) {
            try await service.exchange(units: 1)
        }
        #expect(await service.currentStep().walkingCount == 0)
    }

    @Test("서버 복구 알림을 반영한다")
    func appliesRestore() async {
        let reader = StubReader(batches: [[1]])
        let (service, _) = makeService(reader: reader)
        await service.startCollection()

        await service.applyRestore(walkingCount: 1_000, eventId: "E1")
        await service.applyRestore(walkingCount: 1_000, eventId: "E1")   // 재전달

        #expect(await service.currentStep().walkingCount == 1_000)
    }

    /// 조건이 참이 될 때까지 짧게 기다린다 (관측 Task 가 도는 것을 기다리는 용도).
    private func waitUntil(_ condition: () async -> Bool) async {
        for _ in 0 ..< 100 {
            if await condition() { return }
            try? await Task.sleep(for: .milliseconds(5))
        }
    }
}
