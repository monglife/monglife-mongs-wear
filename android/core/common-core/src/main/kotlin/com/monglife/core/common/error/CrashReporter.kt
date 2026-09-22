package com.monglife.core.common.error

/**
 * 처리된 예외를 외부 수집기로 넘기는 통로.
 *
 * <p>구현(Crashlytics)은 앱 모듈에 둔다. core 는 wear/mobile 이 공유하고 특정 플랫폼
 * 라이브러리를 몰라야 해서, 여기에는 함수 하나만 들고 앱이 기동할 때 꽂아 준다.
 *
 * <p>Crashlytics 는 **잡히지 않은** 예외만 자동으로 모은다. 그런데 이 앱의 예외는 거의
 * 전부 {@code BaseViewModel} 의 CoroutineExceptionHandler 가 삼켜서 화면을 되돌린다 -
 * 앱이 죽지 않으니 수집기에 닿지 않는다. 그걸 여기서 명시적으로 올린다.
 *
 * <p>꽂히기 전(기동 극초반)이나 테스트에서는 아무 일도 하지 않는다. 수집기가 없다고
 * 예외 처리가 멈추면 안 된다.
 */
object CrashReporter {

    @Volatile
    private var delegate: ((Throwable) -> Unit)? = null

    fun install(delegate: (Throwable) -> Unit) {
        this.delegate = delegate
    }

    /** 수집기가 없거나 수집기 자체가 실패해도 호출부를 깨뜨리지 않는다. */
    fun record(throwable: Throwable) {
        runCatching { delegate?.invoke(throwable) }
    }
}
