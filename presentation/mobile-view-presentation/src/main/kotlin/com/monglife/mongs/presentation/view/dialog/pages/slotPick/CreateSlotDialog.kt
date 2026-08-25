package com.monglife.mongs.presentation.view.dialog.pages.slotPick

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.textbox.InputTextBox
import com.monglife.mongs.presentation.view.component.common.textbox.TimeWheel
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.pages.main.component.mainPanel

private const val NAME_MAX = 6
private val HOURS = (0..23).toList()
private val MINUTES = (0..59).toList()

/**
 * 몽 생성.
 *
 * wear 는 이름 / 수면 / 기상을 3개 탭으로 나눴다. 192dp 원형 화면에 셋을 한 번에 못 넣어서다.
 * 880dp 폭에서는 한 줄에 들어가므로 탭 기계장치를 걷어냈다.
 *
 * 상태를 호출부에서 받는 이유:
 * SlotPickViewModel 은 InvalidCreateMongException 에 UiState.Create 를 다시 세팅한다.
 * 그러면 이 컴포저블이 컴포지션을 나갔다 들어오면서 remember 가 초기화된다.
 * 워치에선 6자 이름 하나였지만 폰에서는 이름과 휠 위치 둘까지 날아간다.
 * 호출부가 rememberSaveable 로 잡아 두면 서버 오류에도, 프로세스 사망에도 살아남는다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreateSlotDialog(
    modifier: Modifier = Modifier,
    name: MutableState<String>,
    sleepHour: MutableIntState,
    sleepMinute: MutableIntState,
    wakeHour: MutableIntState,
    wakeMinute: MutableIntState,
    onCreateClick: (String, String, String) -> Unit,
    onCloseClick: () -> Unit,
) {
    val context = LocalContext.current
    var confirmOpen by remember { mutableStateOf(false) }
    val imeVisible = WindowInsets.isImeVisible

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            // 스크림에는 imePadding 을 걸지 않는다. 걸면 키보드 뒤로 맵이 비친다.
            .background(color = Color.Black.copy(alpha = 0.88f))
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .width(if (imeVisible) 600.dp else 640.dp)
                .mainPanel()
                .padding(if (imeVisible) 16.dp else 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (imeVisible) {
                /**
                 * 키보드가 뜨면 이름만 남긴다.
                 * 가로 411dp 화면에서 IME 가 210dp 안팎을 먹는데, 이 형태는 88dp 라 어떤 IME 에서도 들어간다.
                 * 완료 키로 포커스를 놓으면 원래 카드로 돌아온다.
                 */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InputTextBox(
                        modifier = Modifier.weight(1f),
                        text = name.value,
                        placeholder = "최대 ${NAME_MAX}자",
                        changeInput = { if (it.length <= NAME_MAX) name.value = it },
                    )
                    Text(
                        text = "${name.value.length}/$NAME_MAX",
                        fontFamily = DAL_MU_RI,
                        fontSize = 16.sp,
                        color = MongsWhite.copy(alpha = 0.6f),
                    )
                }
            } else {
                Text(
                    text = "새로운 몽",
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = MongsWhite,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Field(label = "이름", modifier = Modifier.weight(1.6f)) {
                        InputTextBox(
                            text = name.value,
                            placeholder = "최대 ${NAME_MAX}자",
                            changeInput = { if (it.length <= NAME_MAX) name.value = it },
                        )
                    }
                    Field(label = "수면", modifier = Modifier.weight(1f)) {
                        TimeWheelPair(sleepHour, sleepMinute)
                    }
                    Field(label = "기상", modifier = Modifier.weight(1f)) {
                        TimeWheelPair(wakeHour, wakeMinute)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    BlueButton(text = "닫기", width = 110, height = 48, fontSize = 15, onClick = onCloseClick)
                    BlueButton(
                        text = "생성",
                        width = 110,
                        height = 48,
                        fontSize = 15,
                        // wear 와 동일한 조건
                        disable = name.value.isBlank() ||
                            (sleepHour.intValue == wakeHour.intValue &&
                                sleepMinute.intValue == wakeMinute.intValue),
                        onClick = {
                            if (name.value.length <= NAME_MAX) {
                                confirmOpen = true
                            } else {
                                Toast.makeText(context, "이름은 최대 ${NAME_MAX}자", Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }
        }

        if (confirmOpen) {
            ConfirmAndCancelDialog(
                text = "새로운 몽을\n생성하시겠습니까?",
                confirm = {
                    if (name.value.length <= NAME_MAX) {
                        onCreateClick(
                            name.value,
                            "%02d:%02d".format(sleepHour.intValue, sleepMinute.intValue),
                            "%02d:%02d".format(wakeHour.intValue, wakeMinute.intValue),
                        )
                    } else {
                        Toast.makeText(context, "이름은 최대 ${NAME_MAX}자", Toast.LENGTH_SHORT).show()
                    }
                },
                cancel = { confirmOpen = false },
            )
        }
    }
}

@Composable
private fun Field(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = DAL_MU_RI,
            fontSize = 14.sp,
            color = MongsWhite.copy(alpha = 0.7f),
        )
        content()
    }
}

@Composable
private fun TimeWheelPair(hour: MutableIntState, minute: MutableIntState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimeWheel(
            modifier = Modifier.weight(1f),
            initValue = hour.intValue,
            valueRange = HOURS,
            changeValue = { hour.intValue = it },
        )
        Text(text = ":", fontFamily = DAL_MU_RI, fontSize = 22.sp, color = MongsWhite)
        TimeWheel(
            modifier = Modifier.weight(1f),
            initValue = minute.intValue,
            valueRange = MINUTES,
            changeValue = { minute.intValue = it },
        )
    }
}
