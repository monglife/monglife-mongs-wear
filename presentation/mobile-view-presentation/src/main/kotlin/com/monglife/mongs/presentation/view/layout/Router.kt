package com.monglife.mongs.presentation.view.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.pages.main.MainView
import com.monglife.mongs.presentation.view.utils.AlwaysOnScreen

@Composable
internal fun Router(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RouterPath.Main.route,
        modifier = modifier,
        route = RouterPath.Root.route
    ) {
//        // 배틀
//        navigation(
//            startDestination = RouterPath.BattleMenu.route,
//            route = RouterPath.BattleNested.route
//        ) {
//            composable(route = RouterPath.BattleMenu.route) {
//                AlwaysOnScreen {
//                    BattleMenuView(navController = navController)
//                }
//            }
//            composable(route = "${RouterPath.BattleMatch.route}/{matchId}/{playerId}") { backStackEntry ->
//                AlwaysOnScreen {
//                    BattleMatchView(
//                        navController = navController,
//                        matchId = backStackEntry.arguments?.getString("matchId")?.toLong(),
//                        playerId = backStackEntry.arguments?.getString("playerId"),
//                    )
//                }
//            }
//        }
//        // 충전
//        navigation(
//            startDestination = RouterPath.ChargeStarPoint.route,
//            route = RouterPath.ChargeNested.route
//        ) {
//            composable(route = RouterPath.ChargeStarPoint.route) {
//                ChargeStarPointView(navController = navController)
//            }
//        }
//        // 컬렉션
//        navigation(
//            startDestination = RouterPath.CollectionMenu.route,
//            route = RouterPath.CollectionNested.route,
//        ) {
//            composable(route = RouterPath.CollectionMenu.route) {
//                CollectionMenuView(navController = navController)
//            }
//            composable(route = RouterPath.CollectionMap.route) {
//                CollectionMapView(navController = navController)
//            }
//            composable(route = RouterPath.CollectionMong.route) {
//                CollectionMongView(navController = navController)
//            }
//        }
//        // 환전
//        navigation(
//            startDestination = RouterPath.ExchangeMenu.route,
//            route = RouterPath.ExchangeNested.route,
//        ) {
//            composable(route = RouterPath.ExchangeMenu.route) {
//                ExchangeMenuView(navController = navController)
//            }
//            composable(route = RouterPath.ExchangeStep.route) {
//                ExchangeStepView(navController = navController)
//            }
//            composable(route = RouterPath.ExchangeStarPoint.route) {
//                ExchangeStarPointView(navController = navController)
//            }
//        }
//        // 취식
//        navigation(
//            startDestination = RouterPath.FeedMenu.route,
//            route = RouterPath.FeedNested.route,
//        ) {
//            composable(route = RouterPath.FeedMenu.route) {
//                FeedMenuView(navController = navController)
//            }
//            composable(route = RouterPath.FeedFood.route) {
//                FeedFoodView(navController = navController)
//            }
//            composable(route = RouterPath.FeedSnack.route) {
//                FeedSnackView(navController = navController)
//            }
//        }
//        // 오류 신고
//        composable(route = RouterPath.Feedback.route) {
//            FeedbackView()
//        }
//        // 도움말
//        composable(route = RouterPath.Help.route) {
//            HelpView()
//        }
//        // 인벤토리
//        composable(route = RouterPath.Inventory.route) {
//            InventoryView(navController = navController)
//        }
        // 메인 페이지
        composable(route = RouterPath.Main.route) {
            AlwaysOnScreen {
                MainView(navController = navController)
            }
        }
//        // 공지사항
//        composable(route = RouterPath.Notice.route) {
//            NoticeView(navController = navController)
//        }
//        // 랜덤 뽑기
//        composable(route = RouterPath.RandomDraw.route) {
//            AlwaysOnScreen {
//                RandomDrawView(navController = navController)
//            }
//        }
//        // 맵 탐색
//        composable(route = RouterPath.SearchMap.route) {
//            AlwaysOnScreen {
//                SearchMapView(navController = navController)
//            }
//        }
//        // 환경 설정
//        composable(route = RouterPath.Setting.route) {
//            SettingView()
//        }
//        // 슬롯 선택
//        composable(route = RouterPath.SlotPick.route) {
//            SlotPickView(navController = navController)
//        }
//        // 훈련
//        navigation(
//            startDestination = RouterPath.TrainingMenu.route,
//            route = RouterPath.TrainingNested.route,
//        ) {
//            composable(route = RouterPath.TrainingMenu.route) {
//                TrainingMenuView(navController = navController)
//            }
//            composable(route = "${RouterPath.TrainingPlay.route}/{trainingCode}") { backStackEntry ->
//                AlwaysOnScreen {
//                    TrainingPlayView(
//                        navController = navController,
//                        trainingCode = backStackEntry.arguments?.getString("trainingCode"),
//                    )
//                }
//            }
//        }
    }
}