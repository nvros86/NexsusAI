package com.nexusai.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = NexusTextPrimary
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> CodeBlockView(block)
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                is MarkdownBlock.Header -> {
                    Text(
                        text = block.text,
                        color = NexusTextPrimary,
                        fontSize = when (block.level) {
                            1 -> 20.sp
                            2 -> 18.sp
                            else -> 16.sp
                        },
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                }
                is MarkdownBlock.ListItem -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (block.ordered) "${block.index}." else "•",
                            color = NexusPurple,
                            fontSize = 14.sp
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(block: MarkdownBlock.CodeBlock) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexusSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = block.language ?: "code",
                color = NexusTextTertiary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(block.code))
                    copied = true
                },
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = if (copied) NexusPurple else NexusTextTertiary,
                    modifier = Modifier.padding(0.dp)
                )
            }
        }

        Text(
            text = block.code,
            color = NexusTextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = NexusPurple
                            )
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("```", i) -> {
                    break
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.split("\n")
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        when {
            line.startsWith("```") -> {
                val language = line.removePrefix("```").trim().ifEmpty { null }
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                i++
                blocks.add(MarkdownBlock.CodeBlock(codeLines.joinToString("\n"), language))
            }
            line.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, line.removePrefix("### ")))
                i++
            }
            line.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, line.removePrefix("## ")))
                i++
            }
            line.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, line.removePrefix("# ")))
                i++
            }
            line.matches(Regex("^\\d+\\.\\s.*")) -> {
                val matchResult = Regex("^(\\d+)\\.\\s(.*)").find(line)
                if (matchResult != null) {
                    val (index, content) = matchResult.destructured
                    blocks.add(MarkdownBlock.ListItem(true, index.toInt(), content))
                }
                i++
            }
            line.startsWith("- ") || line.startsWith("* ") -> {
                blocks.add(MarkdownBlock.ListItem(false, 0, line.removePrefix("- ").removePrefix("* ")))
                i++
            }
            line.isBlank() -> {
                i++
            }
            else -> {
                val paragraphLines = mutableListOf<String>()
                while (i < lines.size && lines[i].isNotBlank() && !lines[i].startsWith("#") && !lines[i].startsWith("```") && !lines[i].matches(Regex("^\\d+\\.\\s.*")) && !lines[i].startsWith("- ") && !lines[i].startsWith("* ")) {
                    paragraphLines.add(lines[i])
                    i++
                }
                if (paragraphLines.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
                }
            }
        }
    }

    return blocks
}

sealed class MarkdownBlock {
    data class CodeBlock(val code: String, val language: String? = null) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class ListItem(val ordered: Boolean, val index: Int, val text: String) : MarkdownBlock()
}
