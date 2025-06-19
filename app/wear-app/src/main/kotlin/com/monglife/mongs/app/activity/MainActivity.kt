package com.monglife.mongs.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.monglife.mongs.presentation.view.assets.MongsTheme
import com.monglife.mongs.presentation.view.layout.LayoutView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * UI 로딩
         */
        setContent {
            MongsTheme {
                LayoutView()
            }
        }
    }
}