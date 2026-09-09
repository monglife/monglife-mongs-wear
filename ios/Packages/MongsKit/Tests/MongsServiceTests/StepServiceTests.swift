import MongsModel
import Testing
@testable import MongsService

/// ViewModel ↔ Service 배선 테스트.
/// Android 의 `ExchangeWalkingCountUseCaseTest.kt` 에 대응한다.

@Suite("걸음 수 서비스")
struct StepServiceTests {

    @Test("환전하면 잔액이 줄고 payPoint 를 돌려준다")
    func exchangeConsumesBalance() async throws {
        let service = SimulatedStepService(initialWalkingCount: 2_450, stepsPerTick: 0)

        let result = try await service.exchange(units: 2)

        #expect(result.payPoint == 200)
        #expect(result.step.walkingCount == 450)
    }

    @Test("잔액보다 많이 환전하면 DeviceError 를 던진다")
    func exchangeRejectsOverdraft() async throws {
        let service = SimulatedStepService(initialWalkingCount: 500, stepsPerTick: 0)

        await #expect(throws: DeviceError.notEnoughWalkingCount(requested: 1_000, balance: 500)) {
            try await service.exchange(units: 1)
        }
    }

    @Test("환전 실패 시 잔액은 그대로다")
    func balanceUntouchedOnFailure() async throws {
        let service = SimulatedStepService(initialWalkingCount: 500, stepsPerTick: 0)

        _ = try? await service.exchange(units: 1)

        #expect(await service.currentStep().walkingCount == 500)
    }

    @Test("수집 시작 전에는 available 이 false 다")
    func unavailableBeforeCollectionStarts() async throws {
        let service = SimulatedStepService(initialWalkingCount: 1_000, stepsPerTick: 0)

        var stream = await service.stepStream().makeAsyncIterator()
        let first = await stream.next()

        #expect(first?.available == false)
        // available 이 false 여도 잔액 자체는 흘러온다. UI 가 "-" 를 그릴 뿐이다.
        #expect(first?.walkingCount == 1_000)
    }

    @Test("수집을 시작하면 available 이 true 로 바뀐 값이 흘러온다")
    func becomesAvailableAfterStart() async throws {
        let service = SimulatedStepService(initialWalkingCount: 1_000, stepsPerTick: 0)

        var stream = await service.stepStream().makeAsyncIterator()
        _ = await stream.next()          // 구독 즉시 오는 현재 값

        await service.startCollection()
        let afterStart = await stream.next()

        #expect(afterStart?.available == true)
    }

    @Test("수집 시작은 멱등이다")
    func startIsIdempotent() async throws {
        let service = SimulatedStepService(initialWalkingCount: 0, stepsPerTick: 0)

        await service.startCollection()
        await service.startCollection()
        await service.startCollection()

        #expect(await service.currentSource() == .simulated)
        #expect(await service.currentStep().walkingCount == 0)
    }
}
