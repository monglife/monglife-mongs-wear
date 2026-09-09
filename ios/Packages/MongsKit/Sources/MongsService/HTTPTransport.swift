import Foundation
import MongsModel

/// HTTP 전송 계층
///
/// `APIClient` 를 네트워크 없이 테스트하기 위한 이음매다.
/// 실제 구현은 `URLSessionTransport`, 테스트는 스텁을 끼운다.
public protocol HTTPTransport: Sendable {
    func send(_ request: URLRequest) async throws -> (Data, HTTPURLResponse)
}

public struct URLSessionTransport: HTTPTransport {

    private let session: URLSession

    /// - Parameters:
    ///   - connectTimeout: Android `*_connect_time_out` (기본 120초)
    ///   - readTimeout: Android `*_read_time_out` (기본 30초)
    ///
    /// URLSession 은 Android/OkHttp 처럼 connect/read/write 를 따로 주지 않는다.
    /// `timeoutIntervalForRequest` 가 "응답이 멈춘 채 버틸 시간"이라 read 에 가깝고,
    /// `timeoutIntervalForResource` 가 요청 전체의 상한이라 connect 값을 얹었다.
    public init(connectTimeout: TimeInterval = 120, readTimeout: TimeInterval = 30) {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.timeoutIntervalForRequest = readTimeout
        configuration.timeoutIntervalForResource = connectTimeout
        configuration.waitsForConnectivity = false
        self.session = URLSession(configuration: configuration)
    }

    public func send(_ request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        do {
            let (data, response) = try await session.data(for: request)
            guard let http = response as? HTTPURLResponse else {
                throw APIError.transport("HTTP 응답이 아니다")
            }
            return (data, http)
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.transport(error.localizedDescription)
        }
    }
}
