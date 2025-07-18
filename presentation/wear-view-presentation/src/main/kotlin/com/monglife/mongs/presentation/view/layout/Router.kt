package com.monglife.mongs.presentation.view.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.navigation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.pages.battle.BattleMatchView
import com.monglife.mongs.presentation.view.pages.battle.BattleMenuView
import com.monglife.mongs.presentation.view.pages.charge.ChargeStarPointView
import com.monglife.mongs.presentation.view.pages.collection.CollectionMapView
import com.monglife.mongs.presentation.view.pages.collection.CollectionMenuView
import com.monglife.mongs.presentation.view.pages.collection.CollectionMongView
import com.monglife.mongs.presentation.view.pages.exchange.ExchangeMenuView
import com.monglife.mongs.presentation.view.pages.exchange.ExchangeStarPointView
import com.monglife.mongs.presentation.view.pages.exchange.ExchangeStepView
import com.monglife.mongs.presentation.view.pages.feed.FeedFoodView
import com.monglife.mongs.presentation.view.pages.feed.FeedMenuView
import com.monglife.mongs.presentation.view.pages.feed.FeedSnackView
import com.monglife.mongs.presentation.view.pages.feedback.FeedbackView
import com.monglife.mongs.presentation.view.pages.help.HelpView
import com.monglife.mongs.presentation.view.pages.inventory.InventoryView
import com.monglife.mongs.presentation.view.pages.main.MainView
import com.monglife.mongs.presentation.view.pages.notice.NoticeView
import com.monglife.mongs.presentation.view.pages.randomDraw.RandomDrawView
import com.monglife.mongs.presentation.view.pages.searchMap.SearchMapView
import com.monglife.mongs.presentation.view.pages.setting.SettingView
import com.monglife.mongs.presentation.view.pages.slotPick.SlotPickView
import com.monglife.mongs.presentation.view.pages.training.TrainingBasketballView
import com.monglife.mongs.presentation.view.pages.training.TrainingMenuView
import com.monglife.mongs.presentation.view.pages.training.TrainingRunnerView
import com.monglife.mongs.presentation.view.utils.AlwaysOnScreen

@Composable
internal fun Router(
    modifier: Modifier = Modifier,
) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = RouterPath.Main.route,
        modifier = modifier,
        route = RouterPath.Root.route
    ) {
        // 배틀
        navigation(
            startDestination = RouterPath.BattleMenu.route,
            route = RouterPath.BattleNested.route
        ) {
            composable(route = RouterPath.BattleMenu.route) {
                AlwaysOnScreen {
                    BattleMenuView(navController = navController)
                }
            }
            composable(route = "${RouterPath.BattleMatch.route}/{matchId}/{playerId}") { backStackEntry ->
                AlwaysOnScreen {
                    BattleMatchView(
                        navController = navController,
                        matchId = backStackEntry.arguments?.getString("matchId")?.toLong(),
                        playerId = backStackEntry.arguments?.getString("playerId"),
                    )
                }
            }
        }
        // 충전
        navigation(
            startDestination = RouterPath.ChargeStarPoint.route,
            route = RouterPath.ChargeNested.route
        ) {
            composable(route = RouterPath.ChargeStarPoint.route) {
                ChargeStarPointView(navController = navController)
            }
        }
        // 컬렉션
        navigation(
            startDestination = RouterPath.CollectionMenu.route,
            route = RouterPath.CollectionNested.route,
        ) {
            composable(route = RouterPath.CollectionMenu.route) {
                CollectionMenuView(navController = navController)
            }
            composable(route = RouterPath.CollectionMap.route) {
                CollectionMapView()
            }
            composable(route = RouterPath.CollectionMong.route) {
                CollectionMongView()
            }
        }
        // 환전
        navigation(
            startDestination = RouterPath.ExchangeMenu.route,
            route = RouterPath.ExchangeNested.route,
        ) {
            composable(route = RouterPath.ExchangeMenu.route) {
                ExchangeMenuView(navController = navController)
            }
            composable(route = RouterPath.ExchangeStep.route) {
                ExchangeStepView(navController = navController)
            }
            composable(route = RouterPath.ExchangeStarPoint.route) {
                ExchangeStarPointView(navController = navController)
            }
        }
        // 취식
        navigation(
            startDestination = RouterPath.FeedMenu.route,
            route = RouterPath.FeedNested.route,
        ) {
            composable(route = RouterPath.FeedMenu.route) {
                FeedMenuView(navController = navController)
            }
            composable(route = RouterPath.FeedFood.route) {
                FeedFoodView(navController = navController)
            }
            composable(route = RouterPath.FeedSnack.route) {
                FeedSnackView(navController = navController)
            }
        }
        // 오류 신고
        composable(route = RouterPath.Feedback.route) {
            FeedbackView()
        }
        // 도움말
        composable(route = RouterPath.Help.route) {
            HelpView()
        }
        // 인벤토리
        composable(route = RouterPath.Inventory.route) {
            InventoryView(navController = navController)
        }
        // 메인 페이지
        composable(route = RouterPath.Main.route) {
            AlwaysOnScreen {
                MainView(navController = navController)
            }
        }
        // 공지사항
        composable(route = RouterPath.Notice.route) {
            NoticeView()
        }
        // 랜덤 뽑기
        composable(route = RouterPath.RandomDraw.route) {
            AlwaysOnScreen {
                RandomDrawView()
            }
        }
        // 맵 탐색
        composable(route = RouterPath.SearchMap.route) {
            AlwaysOnScreen {
                SearchMapView()
            }
        }
        // 환경 설정
        composable(route = RouterPath.Setting.route) {
            SettingView()
        }
        // 슬롯 선택
        composable(route = RouterPath.SlotPick.route) {
            SlotPickView(navController = navController)
        }
        // 훈련
        navigation(
            startDestination = RouterPath.TrainingMenu.route,
            route = RouterPath.TrainingNested.route,
        ) {
            composable(route = RouterPath.TrainingMenu.route) {
                TrainingMenuView(navController = navController)
            }
            composable(route = RouterPath.TrainingRunner.route) {
                AlwaysOnScreen {
                    TrainingRunnerView(navController = navController)
                }
            }
            composable(route = RouterPath.TrainingBasketball.route) {
                AlwaysOnScreen {
                    TrainingBasketballView(navController = navController)
                }
            }
        }
    }
}