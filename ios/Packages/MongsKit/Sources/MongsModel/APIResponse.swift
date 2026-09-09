import Foundation

/// 서버 응답 봉투
///
/// Android `core/data-core/.../web/dto/response/ResponseDto.kt` 이식.
/// 모든 응답이 이 모양이다: `{code, message, httpStatus, result}`.
public struct APIResponse<Result: Decodable & Sendable>: Decodable, Sendable {
    public let code: String?
    public let message: String?
    public let httpStatus: Int?
    public let result: Result
}

/// 페이지 응답 봉투
///
/// Android `PageResponseDto.kt` 이식.
public struct APIPageResponse<Element: Decodable & Sendable>: Decodable, Sendable {
    public let code: String?
    public let message: String?
    public let httpStatus: Int?
    public let result: [Element]
    public let page: Int?
    public let size: Int?
    public let totalPage: Int?
    public let isLastPage: Bool?
}

/// 본문이 없는 응답용 (`Response<Void>` 대응)
public struct EmptyResult: Decodable, Sendable {
    public init() {}
    public init(from decoder: any Decoder) throws {}
}
