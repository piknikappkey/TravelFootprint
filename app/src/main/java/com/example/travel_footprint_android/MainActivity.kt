// app/src/main/java/com/example/travel_footprint_android/MainActivity.kt
package com.example.travel_footprint_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.travel_footprint_android.presentation.screen.JourneyListScreen
import com.example.travel_footprint_android.ui.theme.TravelFootprintTheme
import com.example.travel_footprint_android.utils.DebugHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var debugHelper: DebugHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TravelFootprintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        var showDebugPanel by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            // 正常的主界面
            JourneyListScreen()

            // 调试浮动按钮
            if (showDebugPanel) {
                DebugPanel(
                    debugHelper = debugHelper,
                    onClose = { showDebugPanel = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.Center)
                )
            }

            // 调试开关按钮
            FloatingActionButton(
                onClick = { showDebugPanel = !showDebugPanel },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Text("🔧")
            }
        }
    }

    // MainActivity.kt 中的 DebugPanel 修改为：

    @Composable
    fun DebugPanel(
        debugHelper: DebugHelper,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val scope = rememberCoroutineScope()
        var resultText by remember { mutableStateOf("点击按钮测试足迹数量") }

        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "调试面板 - 足迹数量测试",
                    style = MaterialTheme.typography.titleLarge
                )

                Divider()

                // 测试足迹数量
                Button(
                    onClick = {
                        scope.launch {
                            resultText = "正在测试足迹数量..."
                            debugHelper.testFootprintCounts()
                            resultText = "测试完成，查看 Logcat"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("📊 测试所有旅程足迹数量")
                }

                // 测试单个旅程（需要手动输入ID）
                Button(
                    onClick = {
                        scope.launch {
                            resultText = "正在测试旅程ID=1的足迹数量..."
                            debugHelper.testSingleFootprintCount(1)
                            resultText = "测试完成，查看 Logcat"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔍 测试旅程ID=1的足迹数量")
                }

                // 查看所有旅程
                Button(
                    onClick = {
                        scope.launch {
                            resultText = "正在获取旅程列表..."
                            debugHelper.testGetAllJourneys()
                            resultText = "查看 Logcat"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗺️ 查看所有旅程")
                }

                // 创建测试旅程
                Button(
                    onClick = {
                        scope.launch {
                            resultText = "正在创建测试旅程..."
                            val id = debugHelper.testCreateJourney("测试旅程 ${System.currentTimeMillis()}")
                            resultText = "创建成功: id=$id"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✨ 创建测试旅程")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text("关闭")
                }
                // 添加测试足迹按钮
                Button(
                    onClick = {
                        scope.launch {
                            // 需要先获取旅程ID，这里假设第一个旅程ID=1
                            val journeyId = 1L
                            val footprintId = debugHelper.addTestFootprint(
                                journeyId = journeyId,
                                lat = 39.9042,
                                lng = 116.4074,
                                notes = "测试足迹"
                            )
                            resultText = "足迹添加成功: id=$footprintId"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 添加测试足迹 (旅程ID=1)")
                }

                // MainActivity.kt 的 DebugPanel 中添加
                Button(
                    onClick = {
                        scope.launch {
                            resultText = "正在批量添加测试足迹..."
                            debugHelper.addMultipleTestFootprints()
                            resultText = "批量添加完成，查看 Logcat"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("📍 批量添加测试足迹")
                }
            }
        }
    }
}