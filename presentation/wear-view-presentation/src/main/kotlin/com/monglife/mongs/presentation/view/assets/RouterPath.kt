package com.monglife.mongs.presentation.view.assets

/**
 * 라우팅 경로
 */
sealed class RouterPath(
    val route: String
) {
    // Parent
    data object Root: RouterPath("root")

    // Battle
    data object BattleNested: RouterPath("battle")
    data object BattleMenu: RouterPath("battle/menu")
    data object BattleMatch: RouterPath("battle/match")

    // Charge
    data object ChargeNested: RouterPath("charge")
    data object ChargeStarPoint: RouterPath("charge/starPoint")

    // Collection
    data object CollectionNested: RouterPath("collection")
    data object CollectionMenu: RouterPath("collection/menu")
    data object CollectionMap: RouterPath("collection/map")
    data object CollectionMong: RouterPath("collection/mong")

    // Exchange
    data object ExchangeNested: RouterPath("exchange")
    data object ExchangeMenu: RouterPath("exchange/menu")
    data object ExchangeStep: RouterPath("exchange/step")
    data object ExchangeStarPoint: RouterPath("exchange/starPoint")

    // Feed
    data object FeedNested: RouterPath("feed")
    data object FeedMenu: RouterPath("feed/menu")
    data object FeedFood: RouterPath("feed/food")
    data object FeedSnack: RouterPath("feed/snack")

    // Feedback
    data object Feedback: RouterPath("feedback")

    // Help
    data object Help: RouterPath("help")

    // Inventory
    data object Inventory: RouterPath("inventory")

    // Main
    data object Main: RouterPath("main")

    // Notice
    data object Notice: RouterPath("notice")

    // RandomDraw
    data object RandomDraw: RouterPath("randomDraw")

    // SearchMap
    data object SearchMap: RouterPath("searchMap")

    // Setting
    data object Setting: RouterPath("setting")

    // SlotPick
    data object SlotPick: RouterPath("slotPick")

    // Training
    data object TrainingNested: RouterPath("training")
    data object TrainingMenu: RouterPath("training/menu")
    data object TrainingPlay: RouterPath("training/play")
}
