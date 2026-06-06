package com.example.travel_footprint_android.presentation.components.journey_map.permission_request_content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.SecondColor3

// 权限请求界面组件：当用户未授予位置权限时显示
@Composable
fun PermissionRequestContent(onRequestPermission: () -> Unit) {
    // 全屏背景容器，居中对齐内容
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BGLight0),
        contentAlignment = Alignment.Center
    ) {
        // 垂直排列的权限请求内容，向上偏移 50dp
        Column(
            modifier = Modifier.offset(y = (-50).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 显示地图图标，使用主题色进行着色
            Image(
                modifier = Modifier.fillMaxSize(0.3f),
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "地图图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 显示标题文本
            Headline(
                text = "需要位置权限",
            )
            Spacer(modifier = Modifier.height(10.dp))
            // 显示说明文本，告知用户为何需要位置权限
            TextMedium(
                text = "为了在地图上显示你的位置，\n需要获取设备的位置信息权限",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 授权按钮：点击触发权限请求回调
            ButtonMain(
                onClick = onRequestPermission,
                bgColor = SecondColor3,
                paddingValues = PaddingValues(vertical = 5.dp, horizontal = 10.dp)
            ) {
                Headline(
                    text = "授予位置权限",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}