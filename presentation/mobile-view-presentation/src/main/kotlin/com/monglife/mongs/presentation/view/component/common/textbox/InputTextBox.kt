package com.monglife.mongs.presentation.view.component.common.textbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.pages.main.component.pixelBox

/**
 * 이름 입력.
 *
 * wear 판과 두 가지가 다르다.
 *
 * 1. 비어 있을 때도 innerTextField 를 항상 부른다. wear 는 placeholder 와 둘 중 하나만 그려서
 *    필드가 빈 동안 커서가 보이지 않았다. 워치에선 상관없지만 키보드가 있는 기기에서는
 *    어디에 입력되는지 알 수 없다.
 * 2. 키보드 옵션과 완료 동작을 준다. 완료 시 포커스를 놓아 IME 를 내린다.
 *    LocalSoftwareKeyboardController 대신 LocalFocusManager 를 쓰는 건 전자가 아직 실험 API 라서다.
 */
@Composable
internal fun InputTextBox(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 1,
    textAlign: TextAlign = TextAlign.Start,
    placeholder: String = "",
    icon: Boolean = true,
    readOnly: Boolean = false,
    changeInput: (String) -> Unit = {},
    onDone: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = changeInput,
        textStyle = TextStyle(
            textAlign = textAlign,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            color = MongsWhite,
        ),
        cursorBrush = SolidColor(MongsWhite),
        singleLine = maxLines == 1,
        readOnly = readOnly,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            }
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .pixelBox(
                        fill = Color.Black.copy(alpha = 0.45f),
                        border = MongsWhite.copy(alpha = 0.5f),
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = MongsWhite.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 2.dp),
                    )
                }

                Box(modifier = Modifier.weight(1f).padding(start = if (icon) 10.dp else 0.dp)) {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            textAlign = textAlign,
                            fontFamily = DAL_MU_RI,
                            fontWeight = FontWeight.Light,
                            fontSize = 18.sp,
                            color = MongsWhite.copy(alpha = 0.45f),
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}
