package com.example.travel_footprint_android.presentation2.components.input.input_text

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.FontDark2
import com.example.travel_footprint_android.ui.theme.MainColor1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputText2(
    value: String,
    onValueChange: (String) -> Unit,
    tipText: String,
    maxLength: Int = Int.MAX_VALUE,
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    primaryColor: Color = MainColor1,
    onSurfaceColor: Color = FontDark2,
    containerColor: Color = BGLight2,
    imageVector: ImageVector = Icons.Default.Edit
) {
    BasicTextField(
        value = value,
        onValueChange = { newText ->
            if (newText.length > maxLength) {
                onValueChange(newText.take(maxLength))
            } else {
                onValueChange(newText)
            }
        },
        singleLine = true,
        textStyle = textStyle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .heightIn(min = 56.dp),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = contentPadding, // 关键点：直接控制内边距！
                placeholder = {
                    Text(
                        text = tipText,
                        style = textStyle,
                        color = onSurfaceColor.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
//                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    cursorColor = primaryColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.3f),
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                )
            )
        }
    )
}