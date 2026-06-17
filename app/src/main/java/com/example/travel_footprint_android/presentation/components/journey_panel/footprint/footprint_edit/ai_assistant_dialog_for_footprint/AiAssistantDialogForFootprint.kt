package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.ai_assistant_dialog_for_footprint

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.button.button_border.ButtonBorder
import com.example.travel_footprint_android.presentation.components.custom_scrollbar.VerticalCustomScrollbar
import com.example.travel_footprint_android.presentation.components.dialog.ConfirmDeleteDialog
import com.example.travel_footprint_android.presentation.components.dialog.DialogBox
import com.example.travel_footprint_android.presentation.components.dialog.TipDialog
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.ai_assistant_dialog_for_footprint.components.AiFillFieldForFootprint
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiGenerateState
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiGenerateViewModel
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * 足迹 AI 助手弹窗组件
 *
 * 包含：
 * 1. 右下角 FAB 按钮，点击打开 AI 功能弹窗
 * 2. AI 功能弹窗：仅包含"AI 智能填写"功能
 * 3. 关闭确认弹窗：当 AI 功能正在运行时，关闭前弹出确认提示
 *
 * @param modifier 外部 Modifier
 * @param aiState AI 生成状态（包含加载状态等信息）
 * @param footprint 当前编辑中的足迹数据
 * @param journey 所属旅程数据（提供上下文信息）
 * @param aiGenerateViewModel AI 生成 ViewModel
 * @param onFootprintUpdate 更新足迹数据的回调
 */
@Composable
fun AiAssistantDialogForFootprint(
    modifier: Modifier = Modifier,
    aiState: AiGenerateState,
    footprint: Footprint,
    journey: Journey,
    aiGenerateViewModel: AiGenerateViewModel,
    onFootprintUpdate: (Footprint) -> Unit,
) {
    // AI 功能弹窗是否显示
    var showAiDialog by remember { mutableStateOf(false) }

    // AI 关闭确认弹窗是否显示
    var showCloseConfirmDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
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
            // 关闭弹窗的逻辑：如果 AI 正在运行则弹确认框，否则直接关闭
            val tryClose: () -> Unit = {
                if (aiState.isLoading) {
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
                    footprint = footprint,
                    journey = journey,
                    aiGenerateViewModel = aiGenerateViewModel,
                    onFootprintUpdate = onFootprintUpdate,
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
    }
}

@Composable
private fun Content(
    aiState: AiGenerateState,
    footprint: Footprint,
    journey: Journey,
    aiGenerateViewModel: AiGenerateViewModel,
    onFootprintUpdate: (Footprint) -> Unit,
) {
    // 字段选择状态，默认全选
    var selectedFields by remember { mutableStateOf(AiFillFieldForFootprint.entries.toSet()) }
    
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
            AiGenerateButtonForFootprint(
                isLoading = aiState.isLoading,
                selectedFields = selectedFields,
                onSelectionChange = { selectedFields = it },
                onClick = { customPrompt ->
                    if (selectedFields.isEmpty()) {
                        showSelectTip = true
                    } else {
                        aiGenerateViewModel.generateForFootprint(
                            footprint,
                            journey,
                            selectedFields,
                            customPrompt
                        ) { locationName, latitude, longitude, title, description, rating ->
                            onFootprintUpdate(
                                footprint.copy(
                                    title = if (AiFillFieldForFootprint.TITLE in selectedFields) title else footprint.title,
                                    description = if (AiFillFieldForFootprint.DESCRIPTION in selectedFields) description else footprint.description,
                                    address = if (AiFillFieldForFootprint.ADDRESS in selectedFields) locationName else footprint.address,
                                    latitude = if (AiFillFieldForFootprint.ADDRESS in selectedFields) latitude else footprint.latitude,
                                    longitude = if (AiFillFieldForFootprint.ADDRESS in selectedFields) longitude else footprint.longitude,
                                    rating = if (AiFillFieldForFootprint.RATING in selectedFields) rating else footprint.rating,
                                )
                            )
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        // 自定义垂直滚动条
        Box(
            modifier = Modifier
                .matchParentSize()
        ) {
            VerticalCustomScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            )
        }
    }
    
    // 未选择字段提示弹窗
    if (showSelectTip) {
        TipDialog(
            title = "请选择填充内容",
            message = "请至少选择一项要让 AI 填充的内容（标题/描述/地址/评分）",
            onDismiss = { showSelectTip = false }
        )
    }
}
