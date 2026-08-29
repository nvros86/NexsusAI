package com.nexusai.feature.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextTertiary

@Composable
fun CodeEditor(
    content: String,
    onContentChange: (String) -> Unit,
    language: String = "text",
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lines = content.lines()
    val lineCount = lines.size

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(NexusSurface)
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .verticalScroll(scrollState)
                .padding(end = 8.dp, top = 8.dp)
        ) {
            Text(
                text = (1..lineCount).joinToString("\n"),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = NexusTextTertiary.copy(alpha = 0.5f),
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        BasicTextField(
            value = content,
            onValueChange = { newValue ->
                if (!isReadOnly) {
                    onContentChange(newValue)
                }
            },
            readOnly = isReadOnly,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(8.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = NexusTextPrimary,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(NexusPurple),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            }
        )
    }
}
