//// app/src/main/java/com/example/travel_footprint_android/presentation/screen/JourneyListScreen.kt
//package com.example.travel_footprint_android.presentation.screen
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Map
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.example.travel_footprint_android.data.entity.Journey
//import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun JourneyListScreen(
//    viewModel: JourneyViewModel = hiltViewModel(),
//    onNavigateToMap: (Long) -> Unit = {}
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
//    val footprintCounts by viewModel.footprintCounts.collectAsStateWithLifecycle()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("我的旅程") },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { viewModel.showAddDialog() },
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "添加旅程")
//            }
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            when {
//                uiState.isLoading -> {
//                    LoadingView()
//                }
//                uiState.error != null -> {
//                    ErrorView(
//                        error = uiState.error!!,
//                        onRetry = { viewModel.loadJourneys() }
//                    )
//                }
//                uiState.journeys.isEmpty() -> {
//                    EmptyStateView()
//                }
//                else -> {
//                    JourneyList(
//                        journeys = uiState.journeys,
//                        footprintCounts = footprintCounts,
//                        onJourneyClick = { journey ->
//                            viewModel.selectJourney(journey)
//                            onNavigateToMap(journey.id)
//                        },
//                        onDeleteClick = { journey ->
//                            viewModel.deleteJourney(journey)
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    // 添加旅程对话框
//    if (showAddDialog) {
//        AddJourneyDialog(
//            onDismiss = { viewModel.hideAddDialog() },
//            onConfirm = { title, description ->
//                viewModel.createNewJourney(title, description)
//            }
//        )
//    }
//
//
//}
//
//@Composable
//fun JourneyList(
//    journeys: List<Journey>,
//    footprintCounts: Map<Long, Int>,
//    onJourneyClick: (Journey) -> Unit,
//    onDeleteClick: (Journey) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(
//            items = journeys,
//            key = { it.id }
//        ) { journey ->
//            JourneyCard(
//                journey = journey,
//                footprintCount = footprintCounts[journey.id] ?: 0,  // 传递足迹数量
//                onClick = { onJourneyClick(journey) },
//                onDelete = { onDeleteClick(journey) }
//            )
//        }
//    }
//}
//
//@Composable
//fun JourneyCard(
//    journey: Journey,
//    footprintCount: Int,
//    onClick: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        onClick = onClick,
//        elevation = CardDefaults.cardElevation(4.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // 封面图占位
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(MaterialTheme.colorScheme.primaryContainer)
//                ) {
//                    Icon(
//                        Icons.Default.Map,
//                        contentDescription = null,
//                        modifier = Modifier.fillMaxSize().padding(12.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                Column {
//                    Text(
//                        text = journey.title.ifEmpty { "未命名旅程" },
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = "$footprintCount 个足迹 · ${journey.startDate}",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)  ) {
//                Icon(
//                    Icons.Default.Delete,
//                    contentDescription = "删除",
//                    tint = MaterialTheme.colorScheme.error,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun AddJourneyDialog(
//    onDismiss: () -> Unit,
//    onConfirm: (String, String) -> Unit
//) {
//    var title by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("创建新旅程") },
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                OutlinedTextField(
//                    value = title,
//                    onValueChange = { title = it },
//                    label = { Text("旅程名称") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text("描述（可选）") },
//                    minLines = 3,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    if (title.isNotBlank()) {
//                        onConfirm(title, description)
//                    }
//                },
//                enabled = title.isNotBlank()
//            ) {
//                Text("创建")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("取消")
//            }
//        }
//    )
//}
//
//@Composable
//fun LoadingView() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        CircularProgressIndicator()
//    }
//}
//
//@Composable
//fun ErrorView(error: String, onRetry: () -> Unit) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text(
//                text = error,
//                color = MaterialTheme.colorScheme.error
//            )
//            Button(onClick = onRetry) {
//                Text("重试")
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyStateView() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Icon(
//                Icons.Default.Map,
//                contentDescription = null,
//                modifier = Modifier.size(80.dp),
//                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
//            )
//            Text(
//                text = "暂无旅程",
//                style = MaterialTheme.typography.headlineSmall
//            )
//            Text(
//                text = "点击右下角按钮添加你的第一个旅程",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}