package com.monglife.mongs.data.member.feedback.web.enums

enum class FeedbackTypeCode(
    val feedbackTypeId: Long,
    val feedbackName: String,
    val description: String,
) {
    AUTH(0, "인증", "로그인,로그아웃"),
    BATTLE(1, "배틀", "매칭,배틀,승리보상"),
    TRAINING(2, "훈련", "훈련오류,완료보상"),
    COLLECTION(3, "컬렉션", "컬렉션등록,컬렉션조회"),
    CONFIGURE(4, "앱설정", "걸음수누락,배경설정,권한설정"),
    MANAGEMENT(5, "캐릭터", "상호작용"),
    MEMBER(6, "회원정보", "스타포인트,환전"),
    SLOT(7, "슬롯", "슬롯추가,슬롯삭제,추가슬롯구매"),
    STORE(8, "구매", "스타포인트 충전,인앱상품구매"),
    COMMON(9, "기타", "이외"),
    ;
}