import Foundation
import Testing
@testable import MongsModel

/// 걸음 적립 계산 테스트
///
/// Android `domain/device-domain/src/test/.../StepAccumulatorHealthTest.kt` (12케이스) 중
/// HealthKit 에서도 유효한 것들을 옮겼다. 부팅 마크·자정 창·센서 역행 케이스는
/// HealthKit 앵커가 그 문제들을 없애므로 옮기지 않는다.
@Suite("걸음 적립 계산")
struct StepAccumulatorTests {

    private let anchorA = Data("anchor-a".utf8)
    private let anchorB = Data("anchor-b".utf8)

    @Test("첫 관측은 적립하지 않고 기준선만 잡는다")
    func firstObservationIsBaselineOnly() {
        // 앵커 없이 물으면 HealthKit 은 가진 과거를 전부 준다.
        // 그대로 적립하면 앱 설치 전 걸음이 통째로 입금된다.
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: nil,
            sampleCounts: [3_000, 5_000, 12_000],
            newAnchor: anchorA
        )

        #expect(result.credited == 0)
        #expect(result.anchor == anchorA)
    }

    @Test("기준선을 잡은 뒤에는 샘플을 합산한다")
    func creditsAfterBaseline() {
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: anchorA,
            sampleCounts: [120, 80, 15],
            newAnchor: anchorB
        )

        #expect(result.credited == 215)
        #expect(result.anchor == anchorB)
    }

    @Test("샘플이 없으면 0을 적립하고 앵커만 갱신한다")
    func emptyBatchCreditsNothing() {
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: anchorA,
            sampleCounts: [],
            newAnchor: anchorB
        )

        #expect(result.credited == 0)
        #expect(result.anchor == anchorB)
    }

    @Test("한 배치 상한을 넘으면 잘라낸다")
    func clampsToMaxPerBatch() {
        // 센서 이상값이나 깨진 배치가 지갑을 오염시키는 걸 막는 안전판이다.
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: anchorA,
            sampleCounts: [90_000, 90_000],
            newAnchor: anchorB
        )

        #expect(result.credited == StepAccumulator.maxCreditPerBatch)
    }

    @Test("음수 샘플은 잔액을 깎지 않는다")
    func negativeSamplesDoNotSubtract() {
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: anchorA,
            sampleCounts: [100, -500, 50],
            newAnchor: anchorB
        )

        #expect(result.credited == 150)
    }

    @Test("새 앵커가 없으면 기존 앵커를 유지한다")
    func keepsAnchorWhenQueryReturnsNone() {
        // 앵커를 잃으면 다음 조회가 과거 전체를 다시 끌어온다.
        let result = StepAccumulator.applyHealthSamples(
            currentAnchor: anchorA,
            sampleCounts: [10],
            newAnchor: nil
        )

        #expect(result.anchor == anchorA)
    }
}

/// Android `StepRestoreTest.kt` (5케이스) 이식
@Suite("환전 실패 복구")
struct StepRestoreTests {

    @Test("같은 이벤트를 두 번 반영하지 않는다")
    func idempotentByEventId() {
        // MQTT 는 같은 알림을 두 번 보낼 수 있다.
        let first = StepRestore.apply(appliedEventIds: [], restoreWalkingCount: 1_000, eventId: "E1")
        #expect(first.restoredWalkingCount == 1_000)

        let second = StepRestore.apply(
            appliedEventIds: first.appliedEventIds,
            restoreWalkingCount: 1_000,
            eventId: "E1"
        )
        #expect(second.restoredWalkingCount == 0)
        #expect(second.appliedEventIds == ["E1"])
    }

    @Test("식별자가 비면 반영하지 않는다")
    func rejectsBlankEventId() {
        // 중복을 가려낼 수 없다. 이중 적립보다 반영하지 않는 쪽이 안전하다.
        #expect(StepRestore.apply(appliedEventIds: [], restoreWalkingCount: 500, eventId: "").restoredWalkingCount == 0)
        #expect(StepRestore.apply(appliedEventIds: [], restoreWalkingCount: 500, eventId: "   ").restoredWalkingCount == 0)
    }

    @Test("0 이하는 반영하지 않는다", arguments: [0, -1, -1_000])
    func rejectsNonPositive(amount: Int) {
        #expect(StepRestore.apply(appliedEventIds: [], restoreWalkingCount: amount, eventId: "E1").restoredWalkingCount == 0)
    }

    @Test("이력은 최근 50개만 남긴다")
    func keepsBoundedHistory() {
        var ids: [String] = []
        for index in 0 ..< 60 {
            ids = StepRestore.apply(
                appliedEventIds: ids,
                restoreWalkingCount: 10,
                eventId: "E\(index)"
            ).appliedEventIds
        }

        #expect(ids.count == StepRestore.maxAppliedEventIds)
        #expect(ids.first == "E10")   // 오래된 것부터 밀려난다
        #expect(ids.last == "E59")
    }
}
