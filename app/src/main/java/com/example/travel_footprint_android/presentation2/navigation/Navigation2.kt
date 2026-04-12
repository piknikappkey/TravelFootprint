package com.example.travel_footprint_android.presentation2.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.viewmodel.NavController.CustomNavController

@Composable
fun Navigation2(
    navController: CustomNavController,
    modifier: Modifier = Modifier
) {
    /**
     * 自定义导航
     */
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavPathObj2.list.forEach { NavItem(it.name, Modifier.weight(1f)) }
    }
}
@Composable
fun NavItem(text: String, modifier: Modifier) {
    Box(
        modifier = modifier.padding(5.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),  // 先让 Text 占满宽度
            textAlign = TextAlign.Center         // 文字在 Text 内部居中
        )
    }
}