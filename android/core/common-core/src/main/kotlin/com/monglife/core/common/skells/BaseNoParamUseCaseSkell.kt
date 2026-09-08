package com.monglife.core.common.skells/*

package com.monglife.mongs.core.domain.skells

import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UseCase @Inject constructor(
    // TODO: add port
) : BaseNoParamUseCase<Unit> {

    override suspend fun execute(): MongVo = withContext(Dispatchers.IO) {

    }
}

*/