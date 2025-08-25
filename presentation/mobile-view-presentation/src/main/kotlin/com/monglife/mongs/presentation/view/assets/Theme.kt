package com.monglife.mongs.presentation.view.assets

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import com.mongs.presentation.view.mobile.R

// 폰트
val DAL_MU_RI = FontFamily(Font(R.font.dalmoori))

// 색상
val MongsRed = Color(0xFFE90B0B)
val MongsPink = Color(0xFFFF6B6B)
val MongsPink200 = Color(0xFFFED9D9)
val MongsGreen = Color(0xFF3BE368)
val MongsDarkGreen = Color(0xFF179B3D)
val MongsYellow = Color(0xFFFFDA2D)
val MongsDarkYellow = Color(0xFFFFEB3B)
val MongsBlue = Color(0xFF8DCEFE)
val MongsPurple = Color(0xFFCCA2FE)
val MongsDarkPurple = Color(0xFF8F47E6)
val MongsNavy = Color(0xFF0C4DA2)
val MongsWhite = Color(0xFFFFFFFF)
val MongsLightGray = Color(0xFFF0F0F0)
val MongsDarkGray = Color(0xFF737373)
val MongsDarkBrown = Color(0xFF5D4037)

@Composable
fun MongsTheme(content: @Composable () -> Unit) {
    val typography = Typography(defaultFontFamily = DAL_MU_RI)
    val shapes = Shapes(
        small = RoundedCornerShape(50.dp),
        medium = RoundedCornerShape(50.dp),
        large = RoundedCornerShape(50.dp)
    )

    MaterialTheme(
        typography = typography,
        shapes = shapes,
        content = content
    )
}