package com.monglife.mongs.application.member.notice.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.core.application.wrapper.Page
import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.vo.NoticeVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 공지 사항 목록 조회
 */
class GetNoticesUseCase @Inject constructor(
    private val noticeWebPort: NoticeWebPort,
) : BaseParamUseCase<GetNoticesUseCase.Command, Page<NoticeVo>>() {

    override suspend fun execute(command: Command): Page<NoticeVo> {
        return withContext(Dispatchers.IO) {
            noticeWebPort.getNotices(
                page = command.page,
                size = command.size
            ).let { response ->
                Page(
                    page = response.page,
                    size = response.size,
                    totalPage = response.totalPage,
                    isLastPage = response.isLastPage,
                    result = response.result.map { NoticeVo.of(it.toDomain()) },
                )
            }
        }
    }

    data class Command(
        val page: Int,
        val size: Int,
    )
}