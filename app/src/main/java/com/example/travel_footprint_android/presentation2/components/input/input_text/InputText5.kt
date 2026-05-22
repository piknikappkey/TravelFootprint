package com.example.travel_footprint_android.presentation2.components.input.input_text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.MainColor2
import com.example.travel_footprint_android.ui.theme.SecondColor2

@Composable
fun InputText5(
    value: String,
    onValueChange: (String) -> Unit,
    tipText: String,
    maxLength: Int = Int.MAX_VALUE,
    padding: PaddingValues = PaddingValues(horizontal = 24.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    primaryColor: Color = SecondColor2,
    onSurfaceColor: Color = MainColor2,
    containerColor: Color = BGLight2,
    imageVector: ImageVector = Icons.Default.Edit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) primaryColor else onSurfaceColor.copy(.6f)
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(contentPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,   // 居中对齐
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = { newText ->
                    if (newText.length > maxLength) onValueChange(newText.take(maxLength))
                    else onValueChange(newText)
                },
                singleLine = false,              // ← 允许多行
                textStyle = textStyle,
                cursorBrush = SolidColor(primaryColor),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = tipText,
                            style = textStyle,
                            color = onSurfaceColor.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}