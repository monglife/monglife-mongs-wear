package com.monglife.mongs.presentation.view.assets

import com.mongs.wear.presentation.view.wear.R

/**
 * 사운드 리소스
 */
enum class SoundResourceCode (
    val code: Int,
) {
    BUTTON_CLICK(R.raw.button_click),
    TRAINING_END(R.raw.battle_win),
    BATTLE_MATCHING(R.raw.battle_find),
    BATTLE_WIN(R.raw.battle_win),
    BATTLE_LOSE(R.raw.battle_lose),
    BATTLE_ATTACK(R.raw.battle_attack),
    BATTLE_DEFENCE(R.raw.battle_defence),
    COIN(R.raw.coin),
}