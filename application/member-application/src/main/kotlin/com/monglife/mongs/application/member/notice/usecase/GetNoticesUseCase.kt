package com.monglife.mongs.application.member.notice.usecase

import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.vo.NoticeVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.core.domain.wrapper.Page
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
            // 공지 사항 목록 조회 요청
            noticeWebPort.getNotices(
                page = command.page,
                size = command.size
            ).let {
                val result = it.result.map { response ->
                    // NoticeVo 반환
                    NoticeVo.of(response.toDomain())
                }

                Page(
                    result = result,
                    page = it.page,
                    size = it.size,
                    totalPage = it.totalPage,
                    isLastPage = it.isLastPage,
                )
            }
        }
    }

    data class Command(
        val page: Int,
        val size: Int,
    )
}