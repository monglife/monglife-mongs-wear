package com.monglife.mongs.presentation.view.pages.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.viewmodel.pages.training.basketball.TrainingBasketballViewModel

@Composable
internal fun TrainingBasketballContent(
    modifier: Modifier = Modifier,
    trainingBasketballViewModel: TrainingBasketballViewModel = hiltViewModel(),
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            "농구"
        )

    }
}