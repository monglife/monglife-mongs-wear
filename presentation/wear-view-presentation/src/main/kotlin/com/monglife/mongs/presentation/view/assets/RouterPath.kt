package com.monglife.mongs.presentation.view.assets

/**
 * 라우팅 경로
 */
sealed class RouterPath(
    val route: String
) {
    // Parent
    data object Root: RouterPath("Root")

    // Battle
    data object BattleNested: RouterPath("BattleNested")
    data object BattleMenu: RouterPath("BattleMenu")
    data object BattleMatch: RouterPath("BattleMatch")

    // Charge
    data object ChargeNested: RouterPath("ChargeNested")
    data object ChargeStarPoint: RouterPath("ChargeStarPoint")

    // Collection
    data object CollectionNested: RouterPath("CollectionNested")
    data object CollectionMenu: RouterPath("CollectionMenu")
    data object CollectionMap: RouterPath("CollectionMap")
    data object CollectionMong: RouterPath("CollectionMong")

    // Exchange
    data object ExchangeNested: RouterPath("ExchangeNested")
    data object ExchangeMenu: RouterPath("ExchangeMenu")
    data object ExchangeStep: RouterPath("ExchangeStep")
    data object ExchangeStarPoint: RouterPath("ExchangeStarPoint")

    // Feed
    data object FeedNested: RouterPath("FeedNested")
    data object FeedMenu: RouterPath("FeedMenu")
    data object FeedFood: RouterPath("FeedFood")
    data object FeedSnack: RouterPath("FeedSnack")

    // Feedback
    data object Feedback: RouterPath("Feedback")

    // Help
    data object Help: RouterPath("Help")

    // Inventory
    data object Inventory: RouterPath("Inventory")

    // Main
    data object Main: RouterPath("Main")

    // Notice
    data object Notice: RouterPath("Notice")

    // RandomDraw
    data object RandomDraw: RouterPath("RandomDraw")

    // SearchMap
    data object SearchMap: RouterPath("SearchMap")

    // Setting
    data object Setting: RouterPath("Setting")

    // SlotPick
    data object SlotPick: RouterPath("SlotPick")

    // Training
    data object TrainingNested: RouterPath("TrainingNested")
    data object TrainingMenu: RouterPath("TrainingMenu")
    data object TrainingRunner: RouterPath("TrainingRunner")
    data object TrainingBasketball: RouterPath("TrainingBasketball")
}
