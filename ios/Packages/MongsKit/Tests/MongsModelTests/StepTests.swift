import Testing
@testable import MongsModel

/// Android `domain/device-domain/src/test/.../StepTest.kt`,
/// `StepExchangeRateTest.kt` 에 대응하는 테스트.
/// domain 은 의존성이 0이라 시뮬레이터 없이 `swift test` 만으로 돈다.

@Suite("Step 지갑")
struct StepTests {

    @Test("표시용 걸음 수는 확정 잔액 + 미반영 걸음이다")
    func currentWalkingCountSumsPending() {
        let step = Step(walkingCount: 1_000, pendingWalkingCount: 250)
        #expect(step.currentWalkingCount == 1_250)
    }

    @Test("표시용 걸음 수는 음수로 내려가지 않는다")
    func currentWalkingCountClampsToZero() {
        let step = Step(walkingCount: 100, pendingWalkingCount: -500)
        #expect(step.currentWalkingCount == 0)
    }

    @Test("표시용 걸음 수는 Int32 최대치에서 포화된다")
    func currentWalkingCountClampsToMax() {
        let step = Step(walkingCount: Int(Int32.max), pendingWalkingCount: 1_000)
        #expect(step.currentWalkingCount == Int(Int32.max))
    }

    @Test("미반영 걸음은 환전 대상에서 제외된다")
    func canConsumeIgnoresPending() {
        let step = Step(walkingCount: 1_000, pendingWalkingCount: 5_000)
        #expect(step.canConsume(1_000))
        // pending 까지 환전하면 배치가 끝내 도착하지 않았을 때 잔액이 음수로 밀린다.
        #expect(!step.canConsume(1_001))
    }

    @Test("0 이하는 환전할 수 없다", arguments: [0, -1, -1_000])
    func canConsumeRejectsNonPositive(amount: Int) {
        let step = Step(walkingCount: 10_000)
        #expect(!step.canConsume(amount))
    }

    @Test("환전 단위도 확정 잔액만 본다")
    func exchangeableUnitsIgnoresPending() {
        // pending 5000 을 더하면 7단위가 되지만, 확정 잔액 2450 기준 2단위여야 한다.
        let step = Step(walkingCount: 2_450, pendingWalkingCount: 5_000)
        #expect(step.exchangeableUnits == 2)
    }
}

@Suite("StepExchangeRate 환율")
struct StepExchangeRateTests {

    @Test("1000 걸음 = 1단위 = 100 payPoint")
    func unitConversion() {
        #expect(StepExchangeRate.maxExchangeableUnits(walkingCount: 2_999) == 2)
        #expect(StepExchangeRate.payPoint(units: 2) == 200)
        #expect(StepExchangeRate.walkingCount(units: 2) == 2_000)
    }

    @Test("1000 걸음 미만은 환전 단위가 0이다")
    func belowOneUnit() {
        #expect(StepExchangeRate.maxExchangeableUnits(walkingCount: 999) == 0)
    }

    @Test("음수 입력은 0으로 눌린다")
    func negativeInputsClamp() {
        #expect(StepExchangeRate.maxExchangeableUnits(walkingCount: -5_000) == 0)
        #expect(StepExchangeRate.payPoint(units: -3) == 0)
        #expect(StepExchangeRate.walkingCount(units: -3) == 0)
    }
}

@Suite("StepSource 수집 경로")
struct StepSourceTests {

    @Test("미해결/불가 상태는 수집 중이 아니다")
    func notCollecting() {
        #expect(!StepSource.unresolved.isCollecting)
        #expect(!StepSource.none.isCollecting)
    }

    @Test("HealthKit 경로 판별")
    func healthKitDetection() {
        #expect(StepSource.healthKitAnchored.isHealthKit)
        #expect(StepSource.healthKitDaily.isHealthKit)
        #expect(!StepSource.simulated.isHealthKit)
        #expect(StepSource.simulated.isCollecting)
    }
}
