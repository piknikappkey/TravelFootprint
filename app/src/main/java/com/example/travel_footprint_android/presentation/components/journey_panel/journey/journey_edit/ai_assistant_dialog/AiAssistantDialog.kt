package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.button.button_border.ButtonBorder
import com.example.travel_footprint_android.presentation.components.custom_scrollbar.VerticalCustomScrollbar
import com.example.travel_footprint_android.presentation.components.dialog.DialogBox
import com.example.travel_footprint_android.presentation.components.dialog.TipDialog
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiFillField
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiGenerateState
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiGenerateViewModel
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * AI 助手弹窗组件
 *
 * 包含：
 * 1. 右下角 FAB 按钮，点击打开 AI 功能弹窗
 * 2. AI 功能弹窗：包含"AI 智能填写"和"AI 封面涂鸦"两个功能
 * 3. 无封面提示弹窗：使用涂鸦功能但未选择封面时弹出
 *
 * 后台运行支持：
 * - 关闭弹窗时，若 AI 正在运行，操作仍在后台继续
 * - 通过 onDialogOpenChange 通知 JourneyScreen 控制浮窗显示
 *
 * @param modifier 外部 Modifier
 * @param aiState AI 生成状态（包含加载状态等信息）
 * @param journey 当前编辑中的旅程数据
 * @param aiGenerateViewModel AI 生成 ViewModel
 * @param onJourneyUpdate 更新旅程数据的回调
 * @param onDialogOpenChange 弹窗打开/关闭状态变化回调
 */
@Composable
fun AiAssistantDialog(
    modifier: Modifier = Modifier,
    aiState: AiGenerateState,
    journey: Journey,
    aiGenerateViewModel: AiGenerateViewModel,
    onDialogOpenChange: (Boolean) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onAddressChange: (String, Double, Double) -> Unit = { _, _, _ -> },
    onCoverPathChange: (String) -> Unit = {},
) {
    // AI 功能弹窗是否显示
    var showAiDialog by remember { mutableStateOf(false) }

    // 无封面提示弹窗是否显示
    var showNoCoverTip by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    // 弹窗打开/关闭时通知外部
    LaunchedEffect(showAiDialog) {
        onDialogOpenChange(showAiDialog)
    }

    Box(modifier = modifier) {
        // AI 功能悬浮按钮（FAB），固定在右下角
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(10.dp)
        ) {
            ButtonBorder(
                onClick = { showAiDialog = true },
                paddingValues = PaddingValues(horizontal = 18.dp, 5.dp)
            ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI 功能",
                        tint = MainColor3,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    TextMedium("ai功能")
                }
            }
        }

        // AI 功能弹窗
        if (showAiDialog) {
            DialogBox(
                R.drawable.bg_rectangular_1__2__1,
                R.drawable.bg_rectangular_1__2__2,
                onDismissRequest = { showAiDialog = false }
            ) {
                Content(
                    aiState = aiState,
                    journey = journey,
                    aiGenerateViewModel = aiGenerateViewModel,
                    setShowNoCoverTip = { bool -> showNoCoverTip = bool},
                    onTitleChange = onTitleChange,
                    onDescriptionChange = onDescriptionChange,
                    onAddressChange = onAddressChange,
                    onCoverPathChange = onCoverPathChange,
                )
            }
        }

        // 无封面提示弹窗
        if (showNoCoverTip) {
            TipDialog(
                title = "请先选择封面",
                message = "请先上传一张旅程封面图片，再使用 AI 涂鸦功能",
                onDismiss = { showNoCoverTip = false }
            )
        }
    }
}

@Composable
private fun Content(
    aiState: AiGenerateState,
    journey: Journey,
    aiGenerateViewModel: AiGenerateViewModel,
    setShowNoCoverTip: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onAddressChange: (String, Double, Double) -> Unit = { _, _, _ -> },
    onCoverPathChange: (String) -> Unit = {},
) {
    // 字段选择状态，默认全选
    var selectedFields by remember { mutableStateOf(AiFillField.entries.toSet()) }
    
    // 未选择字段提示弹窗状态
    var showSelectTip by remember { mutableStateOf(false) }

    // 使用 rememberUpdatedState 确保异步回调中始终读取到最新的 journey 值
    // 避免 paintCover 完成时覆盖 generate 已写入的标题/描述/地址
    val currentJourney by rememberUpdatedState(journey)
    
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .heightIn(max = 400.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 10.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 标题
            Headline(
                text = "AI 智能助手",
                modifier = Modifier.padding(bottom = 8.dp, start = 5.dp)
            )

            LineBetween()

            Spacer(Modifier.height(8.dp))

            // AI 智能填写按钮
            AiGenerateButton(
                isLoading = aiState.isLoading,
                selectedFields = selectedFields,
                onSelectionChange = { selectedFields = it },
                onClick = { customPrompt ->
                    if (selectedFields.isEmpty()) {
                        showSelectTip = true
                    } else {
                        aiGenerateViewModel.generate(
                            journey,
                            selectedFields,
                            customPrompt
                        ) { locationName, latitude, longitude, title, description ->
                            if (AiFillField.TITLE in selectedFields) {
                                onTitleChange(title)
                            }
                            if (AiFillField.DESCRIPTION in selectedFields) {
                                onDescriptionChange(description)
                            }
                            if (AiFillField.ADDRESS in selectedFields) {
                                onAddressChange(locationName, latitude, longitude)
                            }
                        }
                    }
                }
            )

            LineBetween()

            // AI 封面涂鸦按钮
            AiPaintButton(
                isLoading = aiState.isPaintLoading,
                onPaintWithPrompt = { prompt ->
                    if (currentJourney.coverImagePath.isBlank()) {
                        setShowNoCoverTip(true)
                    } else {
                        aiGenerateViewModel.paintCover(currentJourney.coverImagePath, prompt) { newPath ->
                            onCoverPathChange(newPath)
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        // 自定义垂直滚动条
        VerticalCustomScrollbar(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd),
        )
    }
    
    // 未选择字段提示弹窗
    if (showSelectTip) {
        TipDialog(
            title = "请选择填充内容",
            message = "请至少选择一项要让 AI 填充的内容（标题/描述/地址）",
            onDismiss = { showSelectTip = false }
        )
    }
}
