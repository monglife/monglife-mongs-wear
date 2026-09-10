import Foundation
import MongsModel
import MongsService
import Observation

/// 충전 ViewModel
///
/// Android `pages/charge/ChargeStarPointViewModel.kt` (326 LOC) 이식.
///
/// 원본이 **중복 소비 버그를 겪고 넣은 장치들**을 그대로 가져왔다. 결제는 되돌릴 수 없으므로
/// 여기서 아끼면 사용자가 돈을 내고 못 받거나 두 번 지급받는다.
@Observable
@MainActor
public final class ChargeViewModel: ErrorReportingViewModel {

    /// 원본 `UiState` — **배타 분기가 아니다.**
    /// 결제 중에는 목록을 유지한 채 위에 덮개를 씌운다.
    public enum Phase: Sendable, Equatable {
        case idle
        /// 최초 진입·재조회. 아직 그릴 게 없다.
        case loading
        /// 결제 진행 중. 목록은 그대로 두고 터치만 막는다.
        case billing

        public var showsLoading: Bool { self != .idle }
        public var showsContent: Bool { self != .loading }
    }

    public private(set) var phase: Phase = .loading
    public private(set) var products: [StoreProduct] = []
    public private(set) var index = 0
    public private(set) var starPoint = 0
    /// 상품이 하나도 없어 화면을 닫아야 하는지
    public private(set) var shouldClose = false

    public var current: StoreProduct? {
        products.indices.contains(index) ? products[index] : nil
    }

    public var isFirst: Bool { index == 0 }
    public var isLast: Bool { index >= products.count - 1 }

    /// 소비를 시도한 주문.
    ///
    /// 실시간 경로(구매 직후)와 회수 경로(자동 소비)가 같은 주문을 각각 잡을 수 있어
    /// 가드를 공유한다. **요청을 보내기 전에** 기록해서, 실패로 빠져나가도
    /// 자동 재시도가 돌지 않게 한다 — 사용자가 "소비" 버튼으로 직접 다시 시도한다.
    private var attemptedOrderIds: Set<String> = []

    private let storeService: StoreService
    private let playerService: PlayerService

    public init(storeService: StoreService, playerService: PlayerService) {
        self.storeService = storeService
        self.playerService = playerService
    }

    public func load() async {
        phase = .loading
        defer { if phase == .loading { phase = .idle } }

        starPoint = await playerService.currentPlayer()?.starPoint ?? 0

        await run {
            self.products = try await self.storeService.products()
            self.index = min(self.index, max(self.products.count - 1, 0))
            if self.products.isEmpty {
                self.shouldClose = true
                return
            }
            await self.autoConsume()
        }
    }

    /// 화면으로 돌아왔을 때. 다른 기기나 승인 대기로 끝난 결제를 회수한다.
    ///
    /// **결제가 진행 중이면 간섭하지 않는다.** 원본도 진행 중인 코루틴을 건드리지 않고
    /// 재조회만 얹는다 — 취소하면 실시간 경로가 죽는다.
    public func refresh() async {
        guard phase != .billing else { return }
        await load()
    }

    public func previous() { index = max(index - 1, 0) }
    public func next() { index = min(index + 1, max(products.count - 1, 0)) }

    /// 구매 → 지급.
    ///
    /// 원본과 같이 **결제 중 재탭을 막는다.** 화면이 보이는 채로 덮개만 씌우기 때문에
    /// 버튼이 눌릴 여지가 남는다.
    public func purchase() async {
        guard phase != .billing, let product = current else { return }
        phase = .billing
        defer { phase = .idle }

        await run {
            let outcome = try await self.storeService.purchase(productId: product.productId)
            switch outcome {
            case let .success(order):
                // 회수 경로가 먼저 잡았으면 여기서는 건너뛴다.
                await self.consumeOnce(order)
                await self.reloadProducts()
            case .cancelled:
                // 사용자가 취소했다. 오류가 아니므로 아무것도 하지 않는다.
                break
            case .pending:
                // 승인이 나면 다음 진입의 자동 회수가 잡는다.
                break
            }
        }
    }

    /// 사용자가 직접 누른 "소비" 재시도.
    ///
    /// 자동 소비 가드를 **우회한다** — 한 번 실패한 주문을 다시 시도하는 유일한 길이다.
    /// 대신 진행 중일 때는 아예 받지 않는다.
    public func consumePending() async {
        guard phase == .idle, let order = current?.orders.first else { return }
        phase = .loading
        defer { phase = .idle }

        await run {
            try await self.storeService.consume(order)
            self.attemptedOrderIds.insert(order.socialOrderId)
            await self.reloadProducts()
        }
    }

    public func recoverFromError(_ error: any Error) async {
        phase = .idle
    }

    // MARK: - 내부

    /// 미소비 주문 자동 소비.
    ///
    /// 진입과 복귀에서만 부른다. 오류 복구 훅에 넣으면
    /// 실패 → 복구 → 재시도 → 실패 의 무한 루프가 된다 (원본이 겪은 것).
    private func autoConsume() async {
        let pending = products.flatMap(\.orders)
        guard !pending.isEmpty else { return }

        var consumedAny = false
        for order in pending where await consumeOnce(order) {
            consumedAny = true
        }
        if consumedAny { await reloadProducts() }
    }

    /// 가드를 통과한 주문만 실제로 보낸다.
    /// - Returns: 요청을 보냈으면 true
    @discardableResult
    private func consumeOnce(_ order: StoreOrder) async -> Bool {
        // 기록이 먼저다. 실패해도 자동 재시도가 돌지 않는다.
        guard attemptedOrderIds.insert(order.socialOrderId).inserted else { return false }
        do {
            try await storeService.consume(order)
            return true
        } catch {
            await ErrorBanner.shared.post(error: error)
            return false
        }
    }

    private func reloadProducts() async {
        starPoint = await playerService.currentPlayer()?.starPoint ?? starPoint
        products = (try? await storeService.products()) ?? products
        index = min(index, max(products.count - 1, 0))
    }
}
