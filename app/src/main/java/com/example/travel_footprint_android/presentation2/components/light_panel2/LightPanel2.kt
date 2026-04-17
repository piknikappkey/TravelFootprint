package com.example.travel_footprint_android.presentation2.components.light_panel2

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LightPanel2(
    modifier: Modifier = Modifier
) {
    /**
     * 面板状态：粗略显示/全部显示/编辑
     */
    val lightPanel2State by remember { mutableStateOf(LightPanel2State.ROUGH_DISPLAY) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        /**
         * 综合面板
         * 功能：
         * 显示点亮的城市/省份内容
         * 编辑点亮的城市/省份内容
         */
        Column {

        }

        /**
         * 点亮城市按钮
         * 点击后面板进入编辑状态
         */
        Button(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                Log.d("LoghtPanel2", "点击点亮城市按钮")
            }
        ) {
            Text("点亮城市")
        }

    }
}