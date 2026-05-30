package com.example.travel_footprint_android.presentation2.components.button.button_draggable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor3
import com.example.travel_footprint_android.ui.theme.SecondColor2

@Composable
fun RainSettingDialog(
    rainEnabled: Boolean,
    onRainEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        BGBox {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Headline(
                    text = "设置",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextMedium(
                        text = "显示涂鸦效果",
                        fontSize = 16.sp,
                    )
                    Switch(
                        checked = rainEnabled,
                        onCheckedChange = onRainEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MainColor3,
                            checkedTrackColor = MainColor3.copy(alpha = 0.5f),
                        )
                    )
                }
                Spacer(Modifier.height(20.dp))
                ButtonSave(
                    title = "关闭",
                    color = SecondColor2,
                    onClick = onDismiss
                )
            }
        }
    }
}
