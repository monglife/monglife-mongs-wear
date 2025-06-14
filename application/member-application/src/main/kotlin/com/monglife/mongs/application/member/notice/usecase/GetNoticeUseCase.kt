package com.monglife.mongs.application.member.notice.usecase

import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.vo.NoticeVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 공지 사항 조회
 */
class GetNoticeUseCase @Inject constructor(
    private val noticeWebPort: NoticeWebPort,
) : BaseParamUseCase<GetNoticeUseCase.Command, NoticeVo>() {

    override suspend fun execute(command: Command): NoticeVo {
        return withContext(Dispatchers.IO) {
            // 공지 사항 조회 요청
            noticeWebPort.getNotice(command.noticeId).let { response ->
                // NoticeVo 반환
                NoticeVo.of(response.toDomain())
            }
        }
    }

    data class Command(
        val noticeId: Long,
    )
}