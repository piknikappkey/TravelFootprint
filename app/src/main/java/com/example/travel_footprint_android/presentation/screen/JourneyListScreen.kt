// app/src/main/java/com/example/travel_footprint_android/presentation/screen/JourneyListScreen.kt
package com.example.travel_footprint_android.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import androidx.compose.material3.CircularProgressIndicator  // 添加这行
import androidx.compose.material3.MaterialTheme  // 确保有这个
import androidx.compose.material3.Button  // 添加这行
import androidx.compose.material3.Text  // 添加这行
import androidx.compose.material3.Card  // 添加这行

@Composable
fun JourneyListScreen(
    viewModel: JourneyViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        !uiState.error.isNullOrBlank() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "错误: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadJourneys() }) {
                        Text("重试")
                    }
                }
            }
        }

        uiState.journeys.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无旅程，点击右上角添加")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.journeys,
                    key = { it.id }  // 添加 key 参数
                ) { journey ->
                    JourneyCard(
                        journey = journey,
                        onClick = { viewModel.selectJourney(journey) }
                    )
                }
            }
        }
    }
}

@Composable
fun JourneyCard(
    journey: Journey,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = journey.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = journey.description.ifEmpty { "无描述" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}