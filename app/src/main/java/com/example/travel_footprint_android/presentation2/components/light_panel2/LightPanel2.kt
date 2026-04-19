package com.example.travel_footprint_android.presentation2.components.light_panel2

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.ui.theme.BGLight1
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.BGLight4
import kotlin.collections.mutableListOf

@Composable
fun LightPanel2(
    modifier: Modifier = Modifier
) {
    /**
     * 面板状态：粗略显示/全部显示/编辑
     */
    var lightPanel2State by remember { mutableStateOf(LightPanel2State.ROUGH_DISPLAY) }

    val lightCityList = remember { mutableListOf<String>("北京市", "山西省", "广西壮族自治区", "湖北省", "江苏省", "天津市", "重庆市", "河北省", "山东省", "四川省", "福建省", "广东省", "湖南省", "河南省", "内蒙古自治区", "台湾省") }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                clip = false  // 不裁剪阴影，保持默认
            )
            // 2. 背景：将形状传给 background，自动带有圆角
            .background(
                color = BGLight2,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
            )
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f) // 设置最大高度
            .wrapContentHeight() // 根据内容调整高度
            .verticalScroll(rememberScrollState()) // 可滚动
    ) {
        /**
         * 综合面板
         * 功能：
         * 显示点亮的城市/省份内容
         * 编辑点亮的城市/省份内容
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    12.dp,
                    8.dp,
                    12.dp,
                    if(lightPanel2State != LightPanel2State.EDIT) 30.dp else 8.dp)
        ) {
            
            /**
             * 复合标题（显示、编辑状态标题）
             * 附带各类功能按钮（展开收起、保存取消）
             */
            PanelTitle(lightPanel2State, lightCityList, { lightPanel2State = it })

            /**
             * 点亮城市内容
             */
            LightCityScreen(lightPanel2State, lightCityList)

            /**
             * 城市编辑模块
             */
            LightCityEditScreen(lightPanel2State)
        }

        /**
         * 点亮城市按钮
         * 点击后面板进入编辑状态
         */
        if( lightPanel2State != LightPanel2State.EDIT) {
            Button(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = {
                    lightPanel2State = LightPanel2State.EDIT
                }
            ) {
                Text("点亮城市")
            }
        }
    }
}