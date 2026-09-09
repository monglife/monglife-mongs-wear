import CoreGraphics
import Foundation
import ImageIO

/// 애니메이션 GIF 를 프레임 배열로 디코드한 결과
///
/// Android 는 Coil 의 `ImageDecoderDecoder` 가 GIF 재생을 통째로 맡지만
/// SwiftUI 에는 GIF 를 그리는 뷰가 없다. 그래서 한 번 디코드해 두고
/// 뷰가 `frame(at:)` 으로 시간에 맞는 프레임을 꺼내 쓰는 구조로 간다.
public struct AnimatedSpriteSource: Sendable {

    public struct Frame: Sendable {
        public let image: CGImage
        /// 이 프레임을 보여줄 시간(초)
        public let duration: TimeInterval
    }

    public let frames: [Frame]
    /// 한 바퀴 도는 데 걸리는 시간(초)
    public let totalDuration: TimeInterval

    public var isEmpty: Bool { frames.isEmpty }

    init(frames: [Frame]) {
        self.frames = frames
        self.totalDuration = frames.reduce(0) { $0 + $1.duration }
    }

    /// 애니메이션 시작 후 `elapsed` 초 시점에 보여줄 프레임
    ///
    /// 뷰가 매 프레임 호출하므로 할당 없이 순회만 한다. 프레임 수는 GIF 당 수십 개 수준이다.
    public func frame(atElapsed elapsed: TimeInterval) -> CGImage? {
        guard !frames.isEmpty else { return nil }
        guard totalDuration > 0 else { return frames[0].image }

        var remainder = elapsed.truncatingRemainder(dividingBy: totalDuration)
        if remainder < 0 { remainder += totalDuration }

        for frame in frames {
            if remainder < frame.duration { return frame.image }
            remainder -= frame.duration
        }
        return frames[frames.count - 1].image
    }
}

extension AnimatedSpriteSource {

    /// GIF(또는 정적 이미지) 데이터를 디코드한다.
    ///
    /// 정적 PNG 도 프레임 1개짜리로 취급한다 — Android `Mong.kt` 의 `isPng` 분기가
    /// 여기서는 필요 없어진다. 뷰는 항상 같은 타입을 받는다.
    public static func decode(data: Data) -> AnimatedSpriteSource? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else { return nil }

        let count = CGImageSourceGetCount(source)
        guard count > 0 else { return nil }

        var frames: [Frame] = []
        frames.reserveCapacity(count)

        for index in 0 ..< count {
            guard let image = CGImageSourceCreateImageAtIndex(source, index, nil) else { continue }
            frames.append(Frame(image: image, duration: frameDuration(source: source, index: index)))
        }

        return frames.isEmpty ? nil : AnimatedSpriteSource(frames: frames)
    }

    /// GIF 프레임의 표시 시간
    ///
    /// `unclampedDelayTime` 을 먼저 본다. 오래된 GIF 는 `delayTime` 에 0 이나 0.01 이 들어 있는데,
    /// 브라우저들이 그런 값을 0.1 로 올려 재생해 온 관례가 있어 원본 애니메이션도 그 속도를
    /// 전제로 만들어졌다. 그대로 쓰면 눈에 띄게 빨라진다.
    private static func frameDuration(source: CGImageSource, index: Int) -> TimeInterval {
        let defaultDuration: TimeInterval = 0.1

        guard
            let properties = CGImageSourceCopyPropertiesAtIndex(source, index, nil) as? [CFString: Any],
            let gif = properties[kCGImagePropertyGIFDictionary] as? [CFString: Any]
        else {
            return defaultDuration
        }

        let unclamped = (gif[kCGImagePropertyGIFUnclampedDelayTime] as? NSNumber)?.doubleValue
        let clamped = (gif[kCGImagePropertyGIFDelayTime] as? NSNumber)?.doubleValue
        let raw = unclamped ?? clamped ?? defaultDuration

        return raw < 0.011 ? defaultDuration : raw
    }
}
