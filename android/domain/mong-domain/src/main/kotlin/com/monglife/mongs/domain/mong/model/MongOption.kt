package com.monglife.mongs.domain.mong.model

class MongOption(
    mongId: Long,
    graduateCheck: Boolean,
) {
    var mongId: Long = mongId
        private set
    var graduateCheck: Boolean = graduateCheck
        private set

    /**
     * 졸업 확인 플래그 변경
     */
    fun graduateCheck() {
        this.graduateCheck = true
    }
}