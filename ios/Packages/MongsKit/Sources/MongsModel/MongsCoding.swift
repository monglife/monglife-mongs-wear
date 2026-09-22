import Foundation

/// 서버와 주고받는 JSON 코덱
///
/// Android `core/data-core/.../global/adapter/GsonLocalDateTimeFormatAdapter.kt` 와
/// `GsonLocalTimeAdapter.kt` 를 그대로 재현한다.
///
/// **이 어댑터들은 비대칭이다.** 쓸 때와 읽을 때 형식이 다르다:
/// - LocalDateTime: 쓰기 `yyyy-MM-dd'T'HH:mm:ss.SSS` / 읽기 ISO_LOCAL_DATE_TIME(밀리초 유무 모두 허용)
/// - LocalTime: 쓰기 `HH:mm:ss` / 읽기 ISO_TIME
///
/// 대칭으로 고치고 싶어지지만 서버가 양쪽을 다르게 취급하고 있을 수 있으므로 그대로 둔다.
///
/// 서버는 타임존 없는 시각(Java `LocalDateTime`)을 보낸다. Swift `Date` 는 절대 시각이라
/// 변환에 타임존이 필요한데, Android 가 기기 로컬로 해석하므로 여기서도 `TimeZone.current` 를 쓴다.
public enum MongsCoding {

    /// 쓰기 전용 형식
    static let dateTimeWriteFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    static let timeWriteFormat = "HH:mm:ss"

    /// 읽기 시 시도할 형식들 (앞에서부터)
    static let dateTimeReadFormats = [
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
    ]
    static let timeReadFormats = [
        "HH:mm:ss.SSS",
        "HH:mm:ss",
        "HH:mm",
    ]

    // 생성 후 절대 변경하지 않는다. 매 호출마다 만들면 비용이 크다.
    private static let cache: [String: DateFormatter] = {
        var formatters: [String: DateFormatter] = [:]
        for format in Set(dateTimeReadFormats + timeReadFormats + [dateTimeWriteFormat, timeWriteFormat]) {
            let formatter = DateFormatter()
            formatter.locale = Locale(identifier: "en_US_POSIX")
            formatter.timeZone = TimeZone.current
            formatter.dateFormat = format
            formatters[format] = formatter
        }
        return formatters
    }()

    static func string(from date: Date, format: String) -> String {
        cache[format]?.string(from: date) ?? ""
    }

    static func date(from string: String, formats: [String]) -> Date? {
        for format in formats {
            if let date = cache[format]?.date(from: string) { return date }
        }
        return nil
    }
}

extension JSONDecoder {

    /// 서버 응답용 디코더
    public static func mongs() -> JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let raw = try decoder.singleValueContainer().decode(String.self)

            // 날짜+시각을 먼저, 실패하면 시각만. 서버가 두 형태를 모두 보낸다.
            if let date = MongsCoding.date(from: raw, formats: MongsCoding.dateTimeReadFormats) {
                return date
            }
            if let date = MongsCoding.date(from: raw, formats: MongsCoding.timeReadFormats) {
                return date
            }
            throw DecodingError.dataCorruptedError(
                in: try decoder.singleValueContainer(),
                debugDescription: "날짜 형식을 해석할 수 없다: \"\(raw)\""
            )
        }
        return decoder
    }
}

extension JSONEncoder {

    /// 서버 요청용 인코더
    public static func mongs() -> JSONEncoder {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .custom { date, encoder in
            var container = encoder.singleValueContainer()
            try container.encode(MongsCoding.string(from: date, format: MongsCoding.dateTimeWriteFormat))
        }
        return encoder
    }
}
