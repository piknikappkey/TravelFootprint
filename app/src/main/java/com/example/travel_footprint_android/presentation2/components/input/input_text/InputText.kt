package com.example.travel_footprint_android.presentation2.components.input.input_text

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.FontDark2
import com.example.travel_footprint_android.ui.theme.MainColor1

@Composable
fun InputText(
    value: String,
    onValueChange: (String) -> Unit,
    tipText: String,
    maxLength: Int = Int.MAX_VALUE,
    textStyle: TextStyle = LocalTextStyle.current,
    primaryColor: Color = MainColor1, // 聚焦时的主题色
    onSurfaceColor: Color = FontDark2, // 普通状态下的文字和描边颜色
    containerColor: Color = BGLight2,  // 输入框内部微妙的背景色
    imageVector: ImageVector = Icons.Default.Edit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newText ->
            // 如果有最大长度限制，则截断超出的部分
            if (newText.length > maxLength) {
                onValueChange(newText.take(maxLength))
            } else {
                onValueChange(newText)
            }
        },
        singleLine = true,

        textStyle = textStyle,

        // 1. 圆角大一点，现代感立现
        shape = RoundedCornerShape(12.dp),

        // 2. 自定义颜色，告别灰扑扑的默认色
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = onSurfaceColor,
            unfocusedTextColor = onSurfaceColor,
            cursorColor = primaryColor,
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.3f), // 边框可见，但颜色柔和
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
        ),

        // 3. 可以加一个小的前导图标，增加细节
        leadingIcon = {
            Icon(
                imageVector = imageVector, // 或者你项目中的图标
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)    // 小一点更精致
            )
        },

        // 4. 占位符文字，提示更友好
        placeholder = {
            Text(
                text = tipText,
                style = textStyle,
                color = onSurfaceColor.copy(alpha = 0.4f)
            )
        },

//        contentPadding = contentPadding,
        // 5. 控制内边距，让文字不顶边
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .height(IntrinsicSize.Min)
            .heightIn(min = 10.dp)  // 保证最小高度，视觉更舒展
    )
}