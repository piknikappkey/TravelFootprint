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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.button.button_border.ButtonBorder
import com.example.travel_footprint_android.presentation.components.custom_scrollbar.VerticalCustomScrollbar
import com.example.travel_footprint_android.presentation.components.dialog.ConfirmDeleteDialog
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
 * 3. 关闭确认弹窗：当 AI 功能正在运行时，关闭前弹出确认提示
 * 4. 无封面提示弹窗：使用涂鸦功能但未选择封面时弹出
 *
 * @param modifier 外部 Modifier
 * @param aiState AI 生成状态（包含加载状态等信息）
 * @param journey 当前编辑中的旅程数据
 * @param aiGenerateViewModel AI 生成 ViewModel
 * @param onJourneyUpdate 更新旅程数据的回调
 */
@Composable
fun AiAssistantDialog(
    modifier: Modifier = Modifier,
    aiState: AiGenerateState,
    journey: Journey,
    aiGenerateViewModel: AiGenerateViewModel,
    onJourneyUpdate: (Journey) -> Unit,
) {
    // AI 功能弹窗是否显示
    var showAiDialog by remember { mutableStateOf(false) }

    // AI 关闭确认弹窗是否显示
    var showCloseConfirmDialog by remember { mutableStateOf(false) }

    // 无封面提示弹窗是否显示
    var showNoCoverTip by remember { mutableStateOf(false) }

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
            // 关闭弹窗的逻辑：如果 AI 正在运行则弹确认框，否则直接关闭
            val tryClose: () -> Unit = {
                if (aiState.isLoading || aiState.isPaintLoading) {
                    showCloseConfirmDialog = true
                } else {
                    showAiDialog = false
                }
            }

            DialogBox(
                R.drawable.bg_rectangular_1__2__1,
                R.drawable.bg_rectangular_1__2__2,
                onDismissRequest = tryClose) {
                Content(
                    aiState = aiState,
                    journey = journey,
                    aiGenerateViewModel = aiGenerateViewModel,
                    onJourneyUpdate = onJourneyUpdate,
                    setShowNoCoverTip = { bool -> showNoCoverTip = bool},
                )
            }
        }

        // AI 关闭确认弹窗
        if (showCloseConfirmDialog) {
            ConfirmDeleteDialog(
                title = "关闭 AI 助手",
                message = "AI 功能正在运行中，关闭弹窗后正在进行的操作将失效，确定关闭吗？",
                onConfirm = {
                    showCloseConfirmDialog = false
                    showAiDialog = false
                },
                onDismiss = { showCloseConfirmDialog = false }
            )
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
    onJourneyUpdate: (Journey) -> Unit,
    setShowNoCoverTip: (Boolean) -> Unit,
) {
    // 字段选择状态，默认全选
    var selectedFields by remember { mutableStateOf(AiFillField.entries.toSet()) }
    
    // 未选择字段提示弹窗状态
    var showSelectTip by remember { mutableStateOf(false) }
    
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
                            onJourneyUpdate(
                                journey.copy(
                                    title = if (AiFillField.TITLE in selectedFields) title else journey.title,
                                    description = if (AiFillField.DESCRIPTION in selectedFields) description else journey.description,
                                    address = if (AiFillField.ADDRESS in selectedFields) locationName else journey.address,
                                    latitude = if (AiFillField.ADDRESS in selectedFields) latitude else journey.latitude,
                                    longitude = if (AiFillField.ADDRESS in selectedFields) longitude else journey.longitude
                                )
                            )
                        }
                    }
                }
            )

            LineBetween()

            // AI 封面涂鸦按钮
            AiPaintButton(
                isLoading = aiState.isPaintLoading,
                onPaintWithPrompt = { prompt ->
                    if (journey.coverImagePath.isBlank()) {
                        setShowNoCoverTip(true)
                    } else {
                        aiGenerateViewModel.paintCover(journey.coverImagePath, prompt) { newPath ->
                            onJourneyUpdate(journey.copy(coverImagePath = newPath))
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
