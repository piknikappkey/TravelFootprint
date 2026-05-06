package com.example.travel_footprint_android.presentation2.components.input.input_text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputText4(
    value: String,                          // 当前显示的值
    onValueChange: (String) -> Unit,        // 值变化回调
    placeholder: String = "请输入内容",
    label: String = "点击输入",
    textStyle: TextStyle = LocalTextStyle.current,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
) {
    // 控制底部彈窗的显示状态
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // 弹窗内的临时文本（避免直接修改外部 value，让用户确认后再提交）
    var inputText by remember { mutableStateOf("") }

    // 焦点请求器，打开弹窗后自动聚焦输入框
    val focusRequester = remember { FocusRequester() }

    // ---------- 假输入框 (点击触发器) ----------
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                inputText = value           // 打开弹窗时，先把当前值复制进来
                showSheet = true
            }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // 这里可以设计成任意你喜欢的外观：一个卡片、一段文字、一个按钮……
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // 显示当前值或占位文本
                Text(
                    text = value.ifEmpty { placeholder },
                    style = textStyle,
                    color = if (value.isEmpty())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // ---------- 真正的输入弹窗 ----------
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                // 用户手动关闭时，可以选择丢弃修改或提交
                showSheet = false
            },
            sheetState = sheetState
        ) {
            // 弹窗内容区域（加上内边距和键盘避让）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp) // 底部留白，让键盘抬起时更舒服
                    .imePadding()             // 关键：自动避开键盘
            ) {
                // 标题（可选）
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 真正的输入框，一出现就自动聚焦
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(placeholder) },
                    textStyle = textStyle,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 确认按钮（可选，也可以直接监听关闭事件自动提交）
                Button(
                    onClick = {
                        onValueChange(inputText)   // 提交修改
                        showSheet = false
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("确定")
                }
            }

            // 弹窗出现后，主动请求焦点（弹出键盘）
            LaunchedEffect(Unit) {
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) { }
            }
        }
    }
}