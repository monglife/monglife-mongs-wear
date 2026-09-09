import Foundation
import MongsModel
import Testing
@testable import MongsService



@Suite("걸음 지갑")
struct StepWalletStoreTests {

    private let anchorA = Data("a".utf8)
    private let anchorB = Data("b".utf8)

    @Test("수집 경로가 다르면 적립을 버린다")
    func sourceGateDropsForeignCredit() async {
        // source gate. 이전 버전이 등록한 관측자가 살아 있거나 권한이 사라져
        // 경로가 none 으로 내려간 뒤에도 알림이 올 수 있다. 그대로 적립하면 이중 카운트다.
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.none)

        let state = await wallet.credit(
            expectedSource: .healthKitAnchored,
            sampleCounts: [500],
            newAnchor: anchorA
        )

        #expect(state.balance == 0)
        #expect(state.anchor == nil)
    }

    @Test("첫 적립은 기준선만 잡는다")
    func firstCreditIsBaseline() async {
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.healthKitAnchored)

        let state = await wallet.credit(
            expectedSource: .healthKitAnchored,
            sampleCounts: [8_000],
            newAnchor: anchorA
        )

        #expect(state.balance == 0)
        #expect(state.anchor == anchorA)
    }

    @Test("기준선 이후 적립이 쌓인다")
    func creditsAccumulate() async {
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.healthKitAnchored)
        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [8_000], newAnchor: anchorA)

        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [120], newAnchor: anchorB)
        let state = await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [80], newAnchor: anchorB)

        #expect(state.balance == 200)
    }

    @Test("경로가 바뀌면 앵커를 버린다")
    func changingSourceClearsAnchor() async {
        // 앵커는 경로마다 의미가 다르다. 남은 값으로 첫 조회를 하면 엉뚱한 델타가 나온다.
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.healthKitAnchored)
        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [1], newAnchor: anchorA)

        let state = await wallet.setSource(.none)

        #expect(state.anchor == nil)
    }

    @Test("같은 경로로 다시 설정하면 앵커를 유지한다")
    func settingSameSourceKeepsAnchor() async {
        // startCollection 은 멱등이라 반복 호출된다. 그때마다 앵커가 날아가면
        // 매번 과거 전체를 다시 읽는다.
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.healthKitAnchored)
        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [1], newAnchor: anchorA)

        let state = await wallet.setSource(.healthKitAnchored)

        #expect(state.anchor == anchorA)
    }

    // MARK: - 차감

    @Test("환전하면 잔액이 줄어든다")
    func consumeReducesBalance() async throws {
        let wallet = await seededWallet(balance: 2_450)

        let state = try await wallet.consume(2_000)

        #expect(state.balance == 450)
    }

    @Test("잔액이 모자라면 던지고 아무것도 바꾸지 않는다")
    func consumeRollsBackWhenInsufficient() async {
        let wallet = await seededWallet(balance: 500)

        await #expect(throws: DeviceError.notEnoughWalkingCount(requested: 1_000, balance: 500)) {
            try await wallet.consume(1_000)
        }
        #expect(await wallet.current().balance == 500)
    }

    // MARK: - 복구

    @Test("복구는 잔액을 되돌린다")
    func restoreAddsBack() async {
        let wallet = await seededWallet(balance: 100)

        let state = await wallet.restore(walkingCount: 1_000, eventId: "E1")

        #expect(state.balance == 1_100)
    }

    @Test("같은 복구 알림이 두 번 와도 한 번만 반영한다")
    func restoreIsIdempotent() async {
        let wallet = await seededWallet(balance: 0)

        await wallet.restore(walkingCount: 1_000, eventId: "E1")
        let state = await wallet.restore(walkingCount: 1_000, eventId: "E1")

        #expect(state.balance == 1_000)
    }

    @Test("복구는 source gate 를 타지 않는다")
    func restoreBypassesSourceGate() async {
        // 복구는 이미 차감했던 걸음을 되돌리는 것이지 센서가 감지한 걸음이 아니다.
        // gate 를 태우면 경로가 none 일 때 영영 복구되지 않는다.
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.none)

        let state = await wallet.restore(walkingCount: 700, eventId: "E1")

        #expect(state.balance == 700)
    }

    // MARK: - 영속

    @Test("앱을 다시 켜도 잔액이 남는다")
    func persistsAcrossInstances() async {
        let store = InMemoryKeyValueStore()
        let first = StepWalletStore(store: store)
        await first.setSource(.healthKitAnchored)
        await first.credit(expectedSource: .healthKitAnchored, sampleCounts: [1], newAnchor: anchorA)
        await first.credit(expectedSource: .healthKitAnchored, sampleCounts: [340], newAnchor: anchorB)

        // 앱 재시작을 흉내낸다
        let second = StepWalletStore(store: store)

        #expect(await second.current().balance == 340)
        #expect(await second.current().anchor == anchorB)
    }

    // MARK: - 도우미

    private func seededWallet(balance: Int) async -> StepWalletStore {
        let wallet = StepWalletStore(store: InMemoryKeyValueStore())
        await wallet.setSource(.healthKitAnchored)
        // 첫 적립은 기준선이므로 두 번 적립해 잔액을 만든다
        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [1], newAnchor: Data("seed".utf8))
        await wallet.credit(expectedSource: .healthKitAnchored, sampleCounts: [balance], newAnchor: Data("seed2".utf8))
        return wallet
    }
}
