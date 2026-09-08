package com.monglife.mongs.presentation.view.component.common.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI

@Composable
internal fun ToggleChip(
    modifier: Modifier = Modifier,
    fontColor: Color,
    backgroundColor: Color,
    label: String,
    checked: Boolean,
    disabled: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Chip(
        modifier = modifier.fillMaxWidth(),
        onClick = { onChanged(!checked) },
        colors = ChipDefaults.chipColors(
            contentColor = fontColor,
            backgroundColor = backgroundColor.copy(alpha = 0.3f)
        ),
        label = {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = label,
                    textAlign = TextAlign.Start,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = fontColor,
                    maxLines = 1,
                    modifier = Modifier.weight(0.8f)
                )

                Switch(
                    checked = checked,
                    onCheckedChange = { onChanged(!checked) },
                    modifier = Modifier.weight(0.2f),
                    enabled = !disabled
                )
            }
        },
    )
}
