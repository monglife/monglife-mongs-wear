package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.port.web.response.CreateMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.DeleteMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.EvolutionMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.GetMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.GraduateMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.PoopCleanMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.SleepingMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.StrokeMongResponseVo
import java.time.LocalTime
import javax.inject.Inject

class ManagementWebAdapter @Inject constructor(

) : ManagementWebPort {

    override suspend fun getMongs(): List<GetMongResponseVo> {
        TODO("Not yet implemented")
    }

    override suspend fun getMong(mongId: Long): GetMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun createMong(
        name: String,
        sleepAt: LocalTime,
        wakeupAt: LocalTime
    ): CreateMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMong(mongId: Long): DeleteMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun strokeMong(mongId: Long): StrokeMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun sleepingMong(mongId: Long): SleepingMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun poopCleanMong(mongId: Long): PoopCleanMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun evolutionMong(mongId: Long): EvolutionMongResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun graduateMong(mongId: Long): GraduateMongResponseVo {
        TODO("Not yet implemented")
    }
}