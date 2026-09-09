import Foundation
import os

/// 로그
///
/// Android `Log.i(TAG, ...)` 자리. `OSLog` 는 릴리스에서 알아서 걸러진다.
enum MongsLog {

    private static let mqttLogger = Logger(subsystem: "com.mongs.wear", category: "MQTT")

    static func mqtt(_ message: String) {
        mqttLogger.debug("MQTT >> \(message, privacy: .public)")
    }
}
