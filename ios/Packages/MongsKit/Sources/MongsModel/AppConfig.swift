import Foundation

/// 실행 프로파일별 설정
///
/// Android 는 `configs/core/data-core/<flavor>/values/config.xml` 의 문자열 리소스를
/// 플레이버별 `res.srcDir` 로 갈아끼워 주입한다.
/// 여기서는 `Configurations/*.xcconfig` → `Info.plist` → 이 구조체로 흐른다.
///
/// 비밀값(MQTT username/password)은 여기에 들어 있지 않다. xcconfig 는 공개 저장소에
/// 커밋되므로, 본 마이그레이션에서 MQTT 를 붙일 때 `android/configs` 서브모듈에
/// iOS 용 디렉토리를 추가해 거기서 읽는다.
public struct AppConfig: Sendable, Equatable {

    public enum Profile: String, Sendable, CaseIterable {
        case local, dev, stg, prd
    }

    public let profile: Profile
    public let discoveryAPIURL: URL
    public let gatewayAPIURL: URL
    public let mqttURL: String
    public let mqttTopic: String
    public let mqttKeepAlive: TimeInterval
    public let connectTimeout: TimeInterval
    public let readTimeout: TimeInterval
    public let writeTimeout: TimeInterval

    public init(
        profile: Profile,
        discoveryAPIURL: URL,
        gatewayAPIURL: URL,
        mqttURL: String,
        mqttTopic: String,
        mqttKeepAlive: TimeInterval,
        connectTimeout: TimeInterval,
        readTimeout: TimeInterval,
        writeTimeout: TimeInterval
    ) {
        self.profile = profile
        self.discoveryAPIURL = discoveryAPIURL
        self.gatewayAPIURL = gatewayAPIURL
        self.mqttURL = mqttURL
        self.mqttTopic = mqttTopic
        self.mqttKeepAlive = mqttKeepAlive
        self.connectTimeout = connectTimeout
        self.readTimeout = readTimeout
        self.writeTimeout = writeTimeout
    }
}

extension AppConfig {

    public enum LoadError: Error, CustomStringConvertible {
        case missingKey(String)
        case invalidValue(key: String, raw: String)

        public var description: String {
            switch self {
            case let .missingKey(key):
                "Info.plist 에 \(key) 가 없다. Configurations/*.xcconfig 와 Info.plist 를 확인해라."
            case let .invalidValue(key, raw):
                "Info.plist 의 \(key) 값을 해석할 수 없다: \"\(raw)\""
            }
        }
    }

    /// 번들의 Info.plist 에서 설정을 읽는다.
    ///
    /// 설정이 깨졌으면 조용히 기본값으로 흘러가는 대신 던진다. 잘못된 서버를
    /// 바라보고 도는 것보다 실행 즉시 죽는 편이 낫다.
    public static func load(from bundle: Bundle = .main) throws -> AppConfig {
        func string(_ key: String) throws -> String {
            guard let raw = bundle.object(forInfoDictionaryKey: key) as? String,
                  !raw.isEmpty else {
                throw LoadError.missingKey(key)
            }
            return raw
        }
        func url(_ key: String) throws -> URL {
            let raw = try string(key)
            guard let url = URL(string: raw), url.scheme != nil else {
                throw LoadError.invalidValue(key: key, raw: raw)
            }
            return url
        }
        func seconds(_ key: String) throws -> TimeInterval {
            let raw = try string(key)
            guard let value = TimeInterval(raw) else {
                throw LoadError.invalidValue(key: key, raw: raw)
            }
            return value
        }

        let rawProfile = try string("MongsProfile")
        guard let profile = Profile(rawValue: rawProfile) else {
            throw LoadError.invalidValue(key: "MongsProfile", raw: rawProfile)
        }

        return AppConfig(
            profile: profile,
            discoveryAPIURL: try url("MongsDiscoveryAPIURL"),
            gatewayAPIURL: try url("MongsGatewayAPIURL"),
            mqttURL: try string("MongsMQTTURL"),
            mqttTopic: try string("MongsMQTTTopic"),
            mqttKeepAlive: try seconds("MongsMQTTKeepAlive"),
            connectTimeout: try seconds("MongsConnectTimeout"),
            readTimeout: try seconds("MongsReadTimeout"),
            writeTimeout: try seconds("MongsWriteTimeout")
        )
    }
}
