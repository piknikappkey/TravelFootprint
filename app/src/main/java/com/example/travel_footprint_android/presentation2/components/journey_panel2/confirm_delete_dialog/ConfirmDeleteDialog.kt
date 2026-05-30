package com.example.travel_footprint_android.presentation2.components.journey_panel2.confirm_delete_dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_delete.ButtonDelete
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor2

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        BGBox {
            BGImgBox(
                listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Headline(
                        text = title,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    TextMedium(
                        text = message,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(15.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(50.dp)
                    ) {
                        ButtonSave(
                            title = "取消",
                            color = SecondColor2,
                            onClick = onDismiss
                        )
                        ButtonDelete(
                            title = "删除!",
                            onClick = onConfirm
                        )
                    }
                }
            }
        }
    }
}
