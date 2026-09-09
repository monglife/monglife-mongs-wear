# Mongs — iOS / watchOS

걸음 수로 키우는 다마고치 서비스 **Mongs** 의 watchOS 클라이언트.
Wear OS 앱([`../android`](../android))을 마이그레이션한다.

- Wear OS 원본: https://play.google.com/store/apps/details?id=com.mongs.wear
- 백엔드: https://github.com/MongLife/monglife-mongs

## 현재 상태

프로젝트 골격 + 걸음 수 화면 데모까지. 서버 연동·HealthKit·MQTT·인증·결제는 아직 없다.
자세한 구조와 남은 작업은 [CLAUDE.md](CLAUDE.md) 참고.

## 시작하기

```bash
brew install xcodegen
cd ios
xcodegen generate
open MongsWear.xcodeproj
```

`.xcodeproj` 는 `project.yml` 에서 생성되므로 커밋하지 않는다. 프로젝트 구성을 바꿀 때는
Xcode GUI 가 아니라 `project.yml` 을 고치고 `xcodegen generate` 를 다시 돌린다.

## 요구 사항

| | 버전 |
|---|---|
| Xcode | 26.6 이상 (watchOS 플랫폼 컴포넌트 포함) |
| watchOS 배포 타겟 | 10.0 |
| Swift | 6.0 language mode (strict concurrency) |
