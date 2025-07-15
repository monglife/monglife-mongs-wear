package com.monglife.mongs.application.member.notice.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.member.notice.exception.NotFoundNoticeException
import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.vo.NoticeVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 공지 사항 조회
 */
class GetNoticeUseCase @Inject constructor(
    private val noticeWebPort: NoticeWebPort,
) : BaseParamUseCase<GetNoticeUseCase.Command, NoticeVo>() {

    @Throws(NotFoundNoticeException::class)
    override suspend fun execute(command: Command): NoticeVo {
        return withContext(Dispatchers.IO) {
            noticeWebPort.getNotice(command.noticeId).let { response ->
                NoticeVo.of(response.toDomain())
            }
        }
    }

    data class Command(
        val noticeId: Long,
    )
}