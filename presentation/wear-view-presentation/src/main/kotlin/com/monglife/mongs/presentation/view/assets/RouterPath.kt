package com.monglife.mongs.presentation.view.assets

/**
 * 라우팅 경로
 */
sealed class RouterPath(
    val route: String
) {

    /* Login */
    data object Login: RouterPath("Login")

    /* MainPager */
    data object MainPager: RouterPath("MainPager")

    /* Feed */
    data object FeedNested: RouterPath("FeedNested")
    data object FeedMenu: RouterPath("FeedMenu")
    data object FeedFoodPick: RouterPath("FeedFoodPick")
    data object FeedSnackPick: RouterPath("FeedSnackPick")

    /* Collection */
    data object CollectionNested: RouterPath("CollectionNested")
    data object CollectionMenu: RouterPath("CollectionMenu")
    data object CollectionMapPick: RouterPath("CollectionMapPick")
    data object CollectionMongPick: RouterPath("CollectionMongPick")

    /* SlotPick */
    data object SlotPick: RouterPath("SlotPick")

    /* ExchangePayPoint */
    data object ExchangeNested: RouterPath("ExchangeNested")
    data object ExchangeMenu: RouterPath("ExchangeMenu")
    data object ExchangeWalking: RouterPath("ExchangeWalking")
    data object ExchangeStarPoint: RouterPath("ExchangeStarPoint")

    /* ChargeStarPoint */
    data object ChargeStarPoint: RouterPath("ChargeStarPoint")

    /* Feedback */
    data object Feedback: RouterPath("Feedback")

    /* Training */
    data object TrainingNested: RouterPath("TrainingNested")
    data object TrainingMenu: RouterPath("TrainingMenu")
    data object TrainingRunner: RouterPath("TrainingRunner")
    data object TrainingBasketball: RouterPath("TrainingBasketball")

    /* Battle */
    data object BattleNested: RouterPath("BattleNested")
    data object BattleMenu: RouterPath("BattleMenu")
    data object BattleMatch: RouterPath("BattleMatch")

    /* Help */
    data object HelpNested: RouterPath("HelpNested")
    data object HelpMenu: RouterPath("HelpMenu")

    /* Setting */
    data object Setting: RouterPath("Setting")

    /* Inventory */
    data object Inventory: RouterPath("Inventory")

    /* Search Map */
    data object SearchMap: RouterPath("SearchMap")

    /* Lucky Draw */
    data object LuckyDraw: RouterPath("LuckyDraw")

    /* Notice */
    data object Notice: RouterPath("Notice")
}
