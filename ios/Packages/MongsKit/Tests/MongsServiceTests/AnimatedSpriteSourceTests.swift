import CoreGraphics
import Foundation
import ImageIO
import Testing
import UniformTypeIdentifiers
@testable import MongsService

/// GIF 디코더 테스트
///
/// 원본 에셋(펫 33종 + 표정 8종)에 의존하지 않도록, 알려진 딜레이를 가진 GIF 를
/// 테스트 안에서 만들어 쓴다.
@Suite("애니메이션 스프라이트 디코드")
struct AnimatedSpriteSourceTests {

    /// 지정한 딜레이(초)를 가진 프레임들로 GIF 데이터를 만든다.
    private func makeGIF(delays: [Double]) throws -> Data {
        let data = NSMutableData()
        let destination = try #require(
            CGImageDestinationCreateWithData(data, UTType.gif.identifier as CFString, delays.count, nil)
        )

        CGImageDestinationSetProperties(destination, [
            kCGImagePropertyGIFDictionary: [kCGImagePropertyGIFLoopCount: 0],
        ] as CFDictionary)

        for (index, delay) in delays.enumerated() {
            let context = try #require(CGContext(
                data: nil, width: 2, height: 2,
                bitsPerComponent: 8, bytesPerRow: 0,
                space: CGColorSpaceCreateDeviceRGB(),
                bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
            ))
            // 프레임마다 다른 색으로 칠한다. 전부 같은 이미지면 GIF 인코더가
            // 중복 프레임을 하나로 합쳐 버려서 프레임 수 검증이 무의미해진다.
            let shade = CGFloat(index + 1) / CGFloat(delays.count + 1)
            context.setFillColor(red: shade, green: 0, blue: 0, alpha: 1)
            context.fill(CGRect(x: 0, y: 0, width: 2, height: 2))
            let image = try #require(context.makeImage())

            CGImageDestinationAddImage(destination, image, [
                kCGImagePropertyGIFDictionary: [kCGImagePropertyGIFDelayTime: delay],
            ] as CFDictionary)
        }

        #expect(CGImageDestinationFinalize(destination))
        return data as Data
    }

    @Test("프레임 수와 총 재생 시간을 읽는다")
    func decodesFramesAndDuration() throws {
        let data = try makeGIF(delays: [0.2, 0.3, 0.5])
        let sprite = try #require(AnimatedSpriteSource.decode(data: data))

        #expect(sprite.frames.count == 3)
        #expect(abs(sprite.totalDuration - 1.0) < 0.01)
    }

    @Test("경과 시간에 맞는 프레임을 고른다")
    func selectsFrameByElapsed() throws {
        let data = try makeGIF(delays: [0.2, 0.3, 0.5])
        let sprite = try #require(AnimatedSpriteSource.decode(data: data))

        // 0.0~0.2 → 0번, 0.2~0.5 → 1번, 0.5~1.0 → 2번
        #expect(sprite.frame(atElapsed: 0.0) === sprite.frames[0].image)
        #expect(sprite.frame(atElapsed: 0.25) === sprite.frames[1].image)
        #expect(sprite.frame(atElapsed: 0.7) === sprite.frames[2].image)
    }

    @Test("총 재생 시간을 넘으면 처음으로 돌아온다")
    func loopsPastTotalDuration() throws {
        let data = try makeGIF(delays: [0.2, 0.3, 0.5])
        let sprite = try #require(AnimatedSpriteSource.decode(data: data))

        // 1.0 초가 한 바퀴 → 1.25 는 0.25 와 같은 프레임
        #expect(sprite.frame(atElapsed: 1.25) === sprite.frame(atElapsed: 0.25))
        #expect(sprite.frame(atElapsed: 10.0) === sprite.frame(atElapsed: 0.0))
    }

    @Test("0에 가까운 딜레이는 0.1초로 올린다")
    func clampsNearZeroDelay() throws {
        // 오래된 GIF 는 delayTime 에 0 이나 0.01 을 넣는데, 브라우저들이 0.1 로 올려
        // 재생해 온 관례가 있어 원본도 그 속도를 전제로 만들어졌다.
        let data = try makeGIF(delays: [0, 0.01])
        let sprite = try #require(AnimatedSpriteSource.decode(data: data))

        #expect(sprite.frames.allSatisfy { $0.duration == 0.1 })
    }

    @Test("정적 이미지도 프레임 1개짜리로 취급한다")
    func decodesStaticImageAsSingleFrame() throws {
        // Android Mong.kt 의 isPng 분기가 여기서는 필요 없어진다.
        let data = try makeGIF(delays: [0.1])
        let sprite = try #require(AnimatedSpriteSource.decode(data: data))

        #expect(sprite.frames.count == 1)
        #expect(sprite.frame(atElapsed: 99) === sprite.frames[0].image)
    }

    @Test("이미지가 아닌 데이터는 nil 을 돌려준다")
    func rejectsGarbage() {
        #expect(AnimatedSpriteSource.decode(data: Data("not an image".utf8)) == nil)
    }
}
