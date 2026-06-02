// app/src/main/java/com/example/travel_footprint_android/presentation2/components/debug/DebugOverlay.kt
package com.example.travel_footprint_android.presentation.components.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.utils.DebugHelper
import kotlinx.coroutines.launch

/**
 * 调试悬浮层
 * 提供一个可开关的调试面板，不污染主界面
 */
@Composable
fun DebugOverlay(
    debugHelper: DebugHelper,
    modifier: Modifier = Modifier
) {
    var showDebugPanel by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 调试面板
        if (showDebugPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                DebugPanel(
                    debugHelper = debugHelper,
                    onClose = { showDebugPanel = false }
                )
            }
        }

        // 调试开关按钮
        FloatingActionButton(
            onClick = { showDebugPanel = !showDebugPanel },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("🔧")
        }
    }
}

@Composable
fun DebugPanel(
    debugHelper: DebugHelper,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var resultText by remember { mutableStateOf("点击按钮测试点亮城市功能") }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🔧 调试面板 - 点亮城市测试", style = MaterialTheme.typography.titleLarge)
            Divider()

            // ==================== 点亮城市测试 ====================
            Text("💡 点亮城市测试", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    scope.launch {
                        resultText = "运行完整点亮城市测试..."
                        debugHelper.testLightCityFull()
                        resultText = "测试完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("🎯 完整点亮城市测试")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "点亮北京市..."
                        debugHelper.testLightCity()
                        resultText = "点亮完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💡 点亮北京市")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "批量点亮城市..."
                        debugHelper.testBatchLightCities()
                        resultText = "批量点亮完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌟 批量点亮城市")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "获取所有点亮城市..."
                        debugHelper.testGetAllLightedCities()
                        resultText = "查询完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 获取所有点亮城市")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "获取点亮城市数量..."
                        debugHelper.testGetLightedCityCount()
                        resultText = "查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔢 获取点亮城市数量")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "检查北京市点亮状态..."
                        debugHelper.testIsCityLighted("110100")
                        resultText = "查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 检查北京市点亮状态")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "取消点亮北京市..."
                        debugHelper.testUnlightCity("110100")
                        resultText = "取消完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("❌ 取消点亮北京市")
            }

            Divider()


            // ==================== 独立点亮省份测试 ====================
            Text("🏛️ 独立点亮省份测试", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    scope.launch {
                        resultText = "运行完整独立点亮省份测试..."
                        debugHelper.testLightProvinceOnly()
                        resultText = "测试完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("🎯 完整独立点亮省份测试")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "独立点亮广东省..."
                        debugHelper.testLightProvinceOnly("440000", "广东省")
                        resultText = "点亮完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🏛️ 独立点亮广东省")
            }

            Button(
                onClick = {
                    scope.launch {
                        resultText = "批量独立点亮省份..."
                        debugHelper.testBatchLightProvincesOnly()
                        resultText = "批量点亮完成，查看 Logcat"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌟 批量独立点亮省份")
            }

            // 状态显示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // 关闭按钮
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("关闭")
            }
        }
    }
}