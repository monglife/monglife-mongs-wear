package com.monglife.mongs.presentation.view.component.common.textbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI

@Composable
internal fun InputTextBox(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 1,
    textAlign: TextAlign = TextAlign.Center,
    placeholder: String = "",
    icon: Boolean = true,
    readOnly: Boolean = false,
    changeInput: (String) -> Unit = {},
) {
    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = { inputName -> changeInput(inputName) },
        textStyle = TextStyle(
            textAlign = textAlign,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.Black,
        ),
        singleLine = maxLines == 1,
        readOnly = readOnly,
        maxLines = maxLines,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.LightGray,
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .padding(all = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = "",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(width = 8.dp))
                }

                if (text.isBlank()) {
                    Text(
                        text = placeholder,
                        textAlign = textAlign,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                } else {
                    innerTextField()
                }
            }
        }
    )
}