package com.nexusai.core.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardHelper {
    fun copyText(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
    }

    fun copyConversation(context: Context, title: String, messages: List<Pair<String, String>>) {
        val formatted = buildString {
            appendLine("# $title")
            appendLine()
            messages.forEach { (role, content) ->
                appendLine("## $role")
                appendLine(content)
                appendLine()
            }
        }
        copyText(context, title, formatted)
    }
}
