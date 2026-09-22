import Foundation
import MQTTNIO
import MongsModel
import NIOCore

/// MQTT 브로커 연결
///
/// Android `core/data-core/.../mqtt/client/MqttClient.kt` (381 LOC) 이식.
///
/// 원본이 Paho + Android 서비스로 하던 일을 `mqtt-nio` 로 옮겼다
/// (CocoaMQTT 는 watchOS 를 지원하지 않는다). 원본이 버그를 겪고 넣은 장치는 그대로 가져왔다:
///
/// - **구독 refcount + 공유 스트림.** 같은 토픽을 두 화면이 보면 브로커 구독은 하나다.
///   원본은 `subscribeCounterMap` + `SharedFlowCache` 로 같은 일을 한다.
/// - **정지 유예 5초.** 마지막 구독자가 사라져도 바로 끊지 않는다. 화면을 옮길 때마다
///   구독 해제 → 재구독 왕복이 생기기 때문이다 (`SharedFlowCache.DEFAULT_STOP_TIMEOUT_MILLIS`).
/// - **재연결 시 재구독.** 원본 `MqttRetryConsumer.onConnectLost` 자리다.
///   Paho 와 달리 mqtt-nio 는 콜백 목록을 지우지 않지만, 브로커 쪽 구독은 사라지므로
///   (`cleanSession = true`) 다시 SUBSCRIBE 를 보내야 한다.
/// - **QoS 2 / cleanSession true / keepAlive 180.** Android 연결 옵션과 같다.
///
/// ⚠️ watchOS 는 백그라운드에서 연결이 끊긴다. 다만 Android 도 `cleanSession = true` 에
/// retained 메시지가 없어서 백그라운드 수신은 원래 안 된다 — 동작 패리티는 유지된다.
public actor MQTTBroker {

    /// 마지막 구독자가 떠난 뒤 실제 UNSUBSCRIBE 까지의 유예.
    /// Android `SharedFlowCache.DEFAULT_STOP_TIMEOUT_MILLIS`.
    private static let stopTimeout: Duration = .seconds(5)

    /// 토픽 하나에 대한 공유 상태
    private struct Subscription {
        var refCount = 0
        var listeners: [UUID: AsyncStream<Data>.Continuation] = [:]
        /// 유예 중인 해제 작업. 새 구독자가 오면 취소한다.
        var teardown: Task<Void, Never>?
    }

    private let config: AppConfig
    private var client: MQTTClient?
    private var subscriptions: [String: Subscription] = [:]
    /// 연결이 진행 중이면 그 작업을 공유한다 — 동시 호출이 커넥션을 여러 개 열지 않도록.
    private var connectTask: Task<MQTTClient, any Error>?

    public init(config: AppConfig) {
        self.config = config
    }

    /// 자격증명이 없으면 브로커를 쓰지 않는다 (`Secrets.local.xcconfig` 가 없는 빌드).
    public var isConfigured: Bool {
        !config.mqttUsername.isEmpty && !config.mqttPassword.isEmpty
    }

    /// 토픽 전체 이름. Android 는 `"${topicPrefix}/mong/management/$mongId"` 로 만든다.
    public nonisolated func topic(_ path: String) -> String {
        "\(config.mqttTopic)/\(path)"
    }

    // MARK: - 구독

    /// 토픽을 구독한다. 스트림이 끝나면(뷰가 사라지면) 구독도 자동으로 정리된다.
    ///
    /// 반환값은 **원본 페이로드**다. 봉투(`ResponseDto`) 해석은 호출하는 서비스가 한다 —
    /// 토픽마다 result 타입이 다르기 때문이다.
    public func subscribe(topic: String) -> AsyncStream<Data> {
        let (stream, continuation) = AsyncStream<Data>.makeStream()
        let id = UUID()

        var entry = subscriptions[topic] ?? Subscription()
        entry.teardown?.cancel()
        entry.teardown = nil
        entry.listeners[id] = continuation
        entry.refCount += 1
        let isFirst = entry.refCount == 1
        subscriptions[topic] = entry

        continuation.onTermination = { [weak self] _ in
            Task { await self?.release(topic: topic, id: id) }
        }

        if isFirst {
            Task { await self.attach(topic: topic) }
        }
        return stream
    }

    private func release(topic: String, id: UUID) {
        guard var entry = subscriptions[topic] else { return }
        guard entry.listeners.removeValue(forKey: id) != nil else { return }
        entry.refCount = max(entry.refCount - 1, 0)

        if entry.refCount == 0 {
            // 바로 끊지 않는다. 화면 전환 중 잠깐 비는 것과 진짜 이탈을 구분할 방법이
            // 없으므로 유예를 둔다 — 원본과 같은 5초.
            entry.teardown = Task { [weak self] in
                try? await Task.sleep(for: Self.stopTimeout)
                guard !Task.isCancelled else { return }
                await self?.detach(topic: topic)
            }
        }
        subscriptions[topic] = entry
    }

    private func attach(topic: String) async {
        do {
            let client = try await connect()
            _ = try await client.subscribe(to: [
                MQTTSubscribeInfo(topicFilter: topic, qos: .exactlyOnce)
            ])
        } catch {
            // 구독 실패는 앱을 막지 않는다. 다음 재연결에서 다시 시도된다.
            // (원본은 지수 백오프로 10회까지 재시도한다. 여기서는 연결 리스너가 그 일을 한다.)
            MongsLog.mqtt("토픽 구독 실패: \(topic) — \(error)")
        }
    }

    private func detach(topic: String) async {
        guard let entry = subscriptions[topic], entry.refCount == 0 else { return }
        subscriptions[topic] = nil
        guard let client, client.isActive() else { return }
        try? await client.unsubscribe(from: [topic])
    }

    private func deliver(topic: String, payload: Data) {
        guard let entry = subscriptions[topic] else { return }
        for continuation in entry.listeners.values { continuation.yield(payload) }
    }

    // MARK: - 연결

    private func connect() async throws -> MQTTClient {
        if let client, client.isActive() { return client }
        if let connectTask { return try await connectTask.value }

        let task = Task<MQTTClient, any Error> { [config] in
            let client = self.client ?? Self.makeClient(config: config)
            await self.adopt(client)

            // cleanSession = true — Android `MqttConnectOptions.isCleanSession` 과 같다.
            // 브로커에 구독이 남지 않으므로 재연결마다 다시 SUBSCRIBE 를 보낸다.
            _ = try await client.connect(cleanSession: true)
            return client
        }
        connectTask = task
        defer { connectTask = nil }
        return try await task.value
    }

    private static func makeClient(config: AppConfig) -> MQTTClient {
        // `tcp://host:port` 에서 host 와 port 를 가른다. Android 는 Paho 가 URL 을 그대로 받지만
        // mqtt-nio 는 나눠서 받는다.
        let stripped = config.mqttURL
            .replacingOccurrences(of: "tcp://", with: "")
            .replacingOccurrences(of: "ssl://", with: "")
        let parts = stripped.split(separator: ":", maxSplits: 1)
        let host = String(parts.first ?? "")
        let port = parts.count > 1 ? Int(parts[1]) ?? 1883 : 1883

        return MQTTClient(
            host: host,
            port: port,
            // 같은 clientId 로 두 번 붙으면 브로커가 앞의 연결을 끊는다. 매번 새로 만든다.
            identifier: "mongs-watch-\(UUID().uuidString)",
            eventLoopGroupProvider: .createNew,
            configuration: .init(
                version: .v3_1_1,
                keepAliveInterval: .seconds(Int64(config.mqttKeepAlive)),
                userName: config.mqttUsername,
                password: config.mqttPassword,
                useSSL: config.mqttURL.hasPrefix("ssl://")
            )
        )
    }

    /// 클라이언트를 처음 만들 때 한 번만 리스너를 건다.
    private func adopt(_ client: MQTTClient) {
        guard self.client !== client else { return }
        self.client = client

        client.addPublishListener(named: "mongs") { [weak self] result in
            guard case let .success(message) = result else { return }
            let topic = message.topicName
            var buffer = message.payload
            guard let bytes = buffer.readBytes(length: buffer.readableBytes) else { return }
            let payload = Data(bytes)
            Task { await self?.deliver(topic: topic, payload: payload) }
        }

        // 연결이 끊기면 브로커 쪽 구독도 사라진다(cleanSession). 다시 붙이고 재구독한다.
        // Android `MqttRetryConsumer.onConnectLost` 가 하던 일이다.
        client.addCloseListener(named: "mongs") { [weak self] _ in
            Task { await self?.reconnect() }
        }
    }

    private func reconnect() async {
        let topics = subscriptions.keys.filter { subscriptions[$0]?.refCount ?? 0 > 0 }
        guard !topics.isEmpty else { return }

        // 즉시 다시 붙으면 브로커가 아직 정리 중일 수 있다. 한 박자 쉰다.
        try? await Task.sleep(for: .seconds(3))
        guard !topics.isEmpty else { return }

        for topic in topics {
            await attach(topic: topic)
        }
    }

    // MARK: - 발행

    /// Android `MqttClient.publish(topic:requestDto:)` — QoS 2, retained false.
    public func publish(topic: String, payload: Data) async throws {
        let client = try await connect()
        try await client.publish(
            to: topic,
            payload: ByteBuffer(bytes: payload),
            qos: .exactlyOnce,
            retain: false
        )
    }

    // MARK: - 종료

    /// 로그아웃 등으로 세션이 끝날 때. 원본 `MqttClient.disconnect()`.
    public func shutdown() async {
        for entry in subscriptions.values {
            entry.teardown?.cancel()
            for continuation in entry.listeners.values { continuation.finish() }
        }
        subscriptions.removeAll()

        guard let client else { return }
        self.client = nil
        if client.isActive() { try? await client.disconnect() }
        try? await client.shutdown()
    }
}
