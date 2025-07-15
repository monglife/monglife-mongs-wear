package com.monglife.mongs.presentation.view.pages.inventory

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.viewmodel.pages.inventory.InventoryViewModel

@Composable
internal fun InventoryView(
    navController: NavController,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = inventoryViewModel.uiState.collectAsState()
    val page = inventoryViewModel.page.collectAsState()
    val size = inventoryViewModel.size.collectAsState()
    val totalPage = inventoryViewModel.totalPage.collectAsState()
    val isLastPage = inventoryViewModel.isLastPage.collectAsState()
    val inventoryVos = inventoryViewModel.inventoryVos.collectAsState()

    Box {
        DefaultBackground()

        if (uiState.value.loadingBar) {
             LoadingBar()
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Log.d("TEST", "page: ${page.value}")
                Log.d("TEST", "size: ${size.value}")
                Log.d("TEST", "totalPage: ${totalPage.value}")
                Log.d("TEST", "isLastPage: ${isLastPage.value}")
                Log.d("TEST", "inventories: ${inventoryVos.value.map { it.inventoryName }}")
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        inventoryViewModel.uiEvent.collect { event ->
            when (event) {
                is InventoryViewModel.UiEvent.NavMenu -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }
                else -> {}
            }
        }
    }
}