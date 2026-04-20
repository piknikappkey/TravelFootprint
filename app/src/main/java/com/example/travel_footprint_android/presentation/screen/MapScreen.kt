// app/src/main/java/com/example/travel_footprint_android/presentation/screen/MapScreen.kt
package com.example.travel_footprint_android.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.travel_footprint_android.presentation.components.map.ChinaMapViewSVG
import com.example.travel_footprint_android.presentation.viewmodel.MapViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun MapScreen(
    journeyId: Long,
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
//    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // 中国地图组件
//        ChinaMapViewSVG(
//            modifier = Modifier.fillMaxSize(),
//            onCityClick = { cityInfo ->
//                viewModel.showAddFootprintDialog(
//                    cityInfo.centerLat.toDouble(),
//                    cityInfo.centerLng.toDouble()
//                )
//            }
//        )
//
//        // 返回按钮
//        IconButton(
//            onClick = onNavigateBack,
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
//        }
//
//        // 添加足迹悬浮按钮
//        FloatingActionButton(
//            onClick = {
//                viewModel.showAddFootprintDialog(39.9042, 116.4074)
//            },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "添加足迹")
//        }
//    }
//
//    // 添加足迹对话框
//    if (showAddDialog) {
//        AddFootprintDialog(
//            viewModel = viewModel,
//            onDismiss = { viewModel.hideAddFootprintDialog() },
//            onConfirm = { title, notes, imagePath ->
//                viewModel.addFootprintWithImage(title, notes, imagePath)
//            }
//        )
//    }
}

@Composable
fun AddFootprintDialog(
    viewModel: MapViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
//    var title by remember { mutableStateOf("") }
//    var notes by remember { mutableStateOf("") }
//    val selectedImagePath by viewModel.selectedImagePath.collectAsStateWithLifecycle()
//    val isImageLoading by viewModel.isImageLoading.collectAsStateWithLifecycle()
//    val context = LocalContext.current
//
//    // 图片选择器（从相册）
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        viewModel.onImageSelected(uri)
//    }
//
//    // 相机拍照
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicturePreview()
//    ) { bitmap: Bitmap? ->
//        bitmap?.let {
//            val path = saveBitmapToFile(context, it)
//            val uri = Uri.fromFile(File(path))
//            viewModel.onImageSelected(uri)
//        }
//    }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("添加足迹") },
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedTextField(
//                    value = title,
//                    onValueChange = { title = it },
//                    label = { Text("标题（可选）") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                OutlinedTextField(
//                    value = notes,
//                    onValueChange = { notes = it },
//                    label = { Text("备注") },
//                    minLines = 3,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                // 图片选择区域
//                Column {
//                    Text("添加图片", style = MaterialTheme.typography.labelMedium)
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        // 从相册选择
//                        OutlinedButton(
//                            onClick = { imagePickerLauncher.launch("image/*") },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Icon(Icons.Default.Image, contentDescription = null)
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("相册")
//                        }
//
//                        // 拍照
//                        OutlinedButton(
//                            onClick = {
//                                cameraLauncher.launch(null)
//                            },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Icon(Icons.Default.CameraAlt, contentDescription = null)
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text("相机")
//                        }
//                    }
//
//                    // 显示已选择的图片
//                    if (isImageLoading) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(100.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    } else if (!selectedImagePath.isNullOrEmpty()) {
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(150.dp)
//                        ) {
//                            AsyncImage(
//                                model = File(selectedImagePath),
//                                contentDescription = "选中的图片",
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    if (notes.isNotBlank()) {
//                        onConfirm(title, notes, selectedImagePath)
//                    }
//                },
//                enabled = notes.isNotBlank()
//            ) {
//                Text("添加")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("取消")
//            }
//        }
//    )
}

/**
 * 保存 Bitmap 到文件
 */
fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
    val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}