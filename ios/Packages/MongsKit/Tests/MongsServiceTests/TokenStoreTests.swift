import Foundation
import MongsModel
import Testing
@testable import MongsService

@Suite("세션 보관소")
struct TokenStoreTests {

    @Test("저장한 세션을 다시 읽는다")
    func savesAndLoads() async {
        let store = TokenStore(store: InMemorySecureStore())
        let session = Session(accountId: 42, accessToken: "a", refreshToken: "r", version: 2)

        await store.save(session)

        #expect(await store.session() == session)
    }

    @Test("저장된 게 없으면 nil 이다")
    func emptyWhenNothingSaved() async {
        let store = TokenStore(store: InMemorySecureStore())
        #expect(await store.session() == nil)
    }

    @Test("삭제하면 사라진다")
    func deletes() async {
        let store = TokenStore(store: InMemorySecureStore())
        await store.save(Session(accountId: 1, accessToken: "a", refreshToken: "r"))

        await store.delete()

        #expect(await store.session() == nil)
    }

    @Test("깨진 값은 세션 없음으로 떨어진다")
    func corruptedValueDegradesToNoSession() async {
        // Android 도 복호화 실패를 예외가 아니라 "세션 없음"으로 처리한다.
        // 로그인 화면으로 보내면 복구되므로 앱을 죽일 이유가 없다.
        let secure = InMemorySecureStore()
        try? secure.set(Data("이건 JSON 이 아니다".utf8), forKey: "session")

        let store = TokenStore(store: secure)

        #expect(await store.session() == nil)
    }

    @Test("앱을 다시 깔면 세션이 사라진다")
    func reinstallClearsSession() async {
        // iOS 는 앱을 삭제해도 Keychain 이 남는다. Android 는 DataStore 가 함께 지워져
        // 재설치하면 로그아웃 상태로 시작하므로 그 동작을 맞춘다.
        let keychain = InMemorySecureStore()          // 앱 삭제에도 살아남는 쪽
        let defaults = InMemoryKeyValueStore()        // 앱과 함께 지워지는 쪽

        await TokenStore(store: keychain, installMarker: defaults)
            .save(Session(accountId: 1, accessToken: "a", refreshToken: "r"))

        // 앱 삭제를 흉내낸다 — Keychain 은 그대로, UserDefaults 만 새것
        let afterReinstall = TokenStore(store: keychain, installMarker: InMemoryKeyValueStore())

        #expect(await afterReinstall.session() == nil)
    }

    @Test("두 번째 실행부터는 세션이 유지된다")
    func normalLaunchKeepsSession() async {
        let keychain = InMemorySecureStore()
        let defaults = InMemoryKeyValueStore()
        let session = Session(accountId: 1, accessToken: "a", refreshToken: "r")

        let first = TokenStore(store: keychain, installMarker: defaults)
        _ = await first.session()          // 첫 실행 — 표식이 찍힌다
        await first.save(session)

        // 앱 재시작 (UserDefaults 는 그대로)
        let second = TokenStore(store: keychain, installMarker: defaults)

        #expect(await second.session() == session)
    }

    @Test("기기 식별자는 한 번 만들면 유지된다")
    func deviceIdentifierIsStable() async {
        // deviceId 는 로그인 키이자 MQTT 토픽에 들어가므로 바뀌면 안 된다.
        let secure = InMemorySecureStore()

        let first = await DeviceIdentifierStore(store: secure).identifier()
        // 앱 재시작을 흉내내려고 스토어 인스턴스를 새로 만든다
        let second = await DeviceIdentifierStore(store: secure).identifier()

        #expect(first == second)
        #expect(!first.isEmpty)
    }
}
