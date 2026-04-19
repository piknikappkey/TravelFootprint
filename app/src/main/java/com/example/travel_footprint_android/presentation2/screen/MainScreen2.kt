package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.travel_footprint_android.presentation2.navigation.CustomNavHost2
import com.example.travel_footprint_android.presentation2.navigation.Navigation2
import com.example.travel_footprint_android.presentation2.viewmodel.NavController.CustomNavController

@Preview
@Composable
fun MainScreen2() {
    /**
     * 导航控制器：CustomNavController（单例模式）
     */

    /**
     * UI设计
     */
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            /**
             * 页面内容
             */
            CustomNavHost2(modifier = Modifier.weight(1f))

            /**
             * 导航内容
             */
            Navigation2(modifier = Modifier.wrapContentHeight())
        }
    }
}