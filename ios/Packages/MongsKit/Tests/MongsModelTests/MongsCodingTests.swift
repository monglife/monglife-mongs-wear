import Foundation
import Testing
@testable import MongsModel

/// 날짜 코덱 테스트
///
/// Android `GsonLocalDateTimeFormatAdapter` / `GsonLocalTimeAdapter` 는 **비대칭**이다.
/// 쓸 때와 읽을 때 형식이 다른데, 그 비대칭을 지키는지가 이 테스트의 요점이다.
@Suite("서버 JSON 코덱")
struct MongsCodingTests {

    private struct Box: Codable, Equatable {
        let at: Date
    }

    private func makeDate(_ components: DateComponents) -> Date {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current
        return calendar.date(from: components)!
    }

    @Test("쓸 때는 밀리초까지 붙인 형식으로 나간다")
    func encodesWithMilliseconds() throws {
        let date = makeDate(DateComponents(year: 2026, month: 3, day: 4, hour: 5, minute: 6, second: 7))
        let json = try JSONEncoder.mongs().encode(Box(at: date))
        let text = String(decoding: json, as: UTF8.self)

        #expect(text.contains("2026-03-04T05:06:07.000"))
    }

    @Test("밀리초가 있는 응답을 읽는다")
    func decodesWithMilliseconds() throws {
        let json = Data(#"{"at":"2026-03-04T05:06:07.123"}"#.utf8)
        let box = try JSONDecoder.mongs().decode(Box.self, from: json)

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current
        #expect(calendar.component(.year, from: box.at) == 2026)
        #expect(calendar.component(.second, from: box.at) == 7)
    }

    @Test("밀리초가 없는 응답도 읽는다")
    func decodesWithoutMilliseconds() throws {
        // 서버가 ISO_LOCAL_DATE_TIME 으로 쓰기 때문에 밀리초가 0이면 생략된다.
        // 쓰기 형식만 지원하면 여기서 깨진다 — 비대칭을 지켜야 하는 이유다.
        let json = Data(#"{"at":"2026-03-04T05:06:07"}"#.utf8)
        let box = try JSONDecoder.mongs().decode(Box.self, from: json)

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current
        #expect(calendar.component(.minute, from: box.at) == 6)
    }

    @Test("시각만 있는 값도 읽는다")
    func decodesTimeOnly() throws {
        // 알림 시각 같은 LocalTime 필드가 이 경로로 온다.
        let json = Data(#"{"at":"21:30:00"}"#.utf8)
        let box = try JSONDecoder.mongs().decode(Box.self, from: json)

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone.current
        #expect(calendar.component(.hour, from: box.at) == 21)
        #expect(calendar.component(.minute, from: box.at) == 30)
    }

    @Test("해석할 수 없는 값은 디코딩 오류를 낸다")
    func rejectsUnknownFormat() {
        let json = Data(#"{"at":"04/03/2026"}"#.utf8)
        #expect(throws: (any Error).self) {
            try JSONDecoder.mongs().decode(Box.self, from: json)
        }
    }
}

@Suite("응답 봉투")
struct APIResponseTests {

    private struct Player: Decodable, Sendable, Equatable {
        let accountId: Int64
        let starPoint: Int
    }

    @Test("result 를 꺼낸다")
    func decodesEnvelope() throws {
        let json = Data("""
        {"code":"OK","message":null,"httpStatus":200,
         "result":{"accountId":7,"starPoint":1200}}
        """.utf8)

        let response = try JSONDecoder.mongs().decode(APIResponse<Player>.self, from: json)

        #expect(response.result.accountId == 7)
        #expect(response.result.starPoint == 1_200)
        #expect(response.code == "OK")
    }

    @Test("페이지 응답의 메타데이터를 읽는다")
    func decodesPageEnvelope() throws {
        let json = Data("""
        {"code":"OK","httpStatus":200,"result":[{"accountId":1,"starPoint":0}],
         "page":0,"size":20,"totalPage":3,"isLastPage":false}
        """.utf8)

        let response = try JSONDecoder.mongs().decode(APIPageResponse<Player>.self, from: json)

        #expect(response.result.count == 1)
        #expect(response.totalPage == 3)
        #expect(response.isLastPage == false)
    }
}

@Suite("세션")
struct SessionTests {

    @Test("재발급하면 세대가 하나 올라간다")
    func reissueBumpsVersion() {
        let session = Session(accountId: 1, accessToken: "a", refreshToken: "r", version: 4)
        let next = session.reissued(accessToken: "a2", refreshToken: "r2")

        #expect(next.version == 5)
        #expect(next.accessToken == "a2")
        #expect(next.accountId == 1)
    }
}

@Suite("API 오류")
struct APIErrorTests {

    @Test("미가입 코드를 알아본다")
    func detectsNeedJoin() {
        let error = APIError.server(code: "DISCOVERY-ACCOUNT-101", message: nil, httpStatus: 400)
        #expect(error.isNeedJoin)
    }

    @Test("인증/인가 오류는 배너로 띄우지 않는다")
    func authErrorsAreNotBannered() {
        // 화면 전환(로그인 화면으로 보내기)으로 처리하는 게 맞다.
        #expect(!APIError.unauthenticated.isMessageShown)
        #expect(!APIError.unauthorized.isMessageShown)
        #expect(APIError.serverUnreachable(statusCode: 500).isMessageShown)
    }
}
