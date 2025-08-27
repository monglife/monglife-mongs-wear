package com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.vo

import com.monglife.mongs.presentation.viewmodel.pages.training.rockPaperScissors.enums.RockPaperScissorsPickCode

data class RockPaperScissorsVo(
    val randomPickCode: RockPaperScissorsPickCode?,
    val pickCode: RockPaperScissorsPickCode?,
    val result: Int,
    val score: Int,
)
