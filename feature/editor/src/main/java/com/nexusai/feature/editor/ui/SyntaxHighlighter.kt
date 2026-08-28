package com.nexusai.feature.editor.ui

import androidx.compose.ui.graphics.Color
import com.nexusai.core.ui.theme.AIBlue
import com.nexusai.core.ui.theme.AIGreen
import com.nexusai.core.ui.theme.AIOrange
import com.nexusai.core.ui.theme.AIPurple

data class SyntaxToken(
    val text: String,
    val color: Color,
    val start: Int,
    val end: Int
)

object SyntaxHighlighter {

    private val keywords = setOf(
        "fun", "val", "var", "class", "interface", "object", "enum", "sealed",
        "data", "object", "companion", "abstract", "open", "final", "override",
        "private", "protected", "internal", "public", "inline", "noinline",
        "suspend", "operator", "infix", "extension", "tailrec", "external",
        "return", "if", "else", "when", "for", "while", "do", "break", "continue",
        "try", "catch", "finally", "throw", "is", "as", "in", "by", "this",
        "super", "true", "false", "null", "package", "import", "typealias",
        "println", "print", "listOf", "mapOf", "setOf", "arrayOf", "mutableListOf",
        "String", "Int", "Long", "Float", "Double", "Boolean", "Char", "Byte",
        "Array", "List", "Map", "Set", "MutableList", "MutableMap", "MutableSet",
        "override", "lateinit", "const", "val", "var", "fun"
    )

    private val kotlinKeywords = keywords + setOf(
        "suspend", "coroutineScope", "launch", "async", "await",
        "flow", "StateFlow", "MutableStateFlow", "collect",
        "viewModelScope", "Dispatchers", "withContext", "runBlocking"
    )

    private val pythonKeywords = setOf(
        "def", "class", "import", "from", "return", "if", "elif", "else",
        "for", "while", "break", "continue", "try", "except", "finally",
        "with", "as", "pass", "lambda", "yield", "raise", "True", "False",
        "None", "and", "or", "not", "in", "is", "print", "self", "global",
        "nonlocal", "assert", "del", "global", "nonlocal"
    )

    private val jsKeywords = setOf(
        "var", "let", "const", "function", "return", "if", "else", "for",
        "while", "do", "break", "continue", "switch", "case", "default",
        "try", "catch", "finally", "throw", "new", "this", "class", "extends",
        "import", "export", "from", "default", "async", "await", "true",
        "false", "null", "undefined", "typeof", "instanceof", "in", "of"
    )

    fun highlight(code: String, language: String): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        val keywordsForLanguage = when (language) {
            "kotlin" -> kotlinKeywords
            "python" -> pythonKeywords
            "javascript", "typescript" -> jsKeywords
            else -> kotlinKeywords
        }

        var i = 0
        val lines = code.lines()

        for (line in lines) {
            var pos = 0
            while (pos < line.length) {
                // Skip whitespace
                if (line[pos].isWhitespace()) {
                    pos++
                    continue
                }

                // Single-line comment
                if (line[pos] == '/' && pos + 1 < line.length && line[pos + 1] == '/') {
                    val start = i + pos
                    tokens.add(SyntaxToken(
                        text = line.substring(pos),
                        color = Color.Gray,
                        start = start,
                        end = start + line.substring(pos).length
                    ))
                    break
                }

                // Hash comment (Python)
                if (line[pos] == '#' && language == "python") {
                    val start = i + pos
                    tokens.add(SyntaxToken(
                        text = line.substring(pos),
                        color = Color.Gray,
                        start = start,
                        end = start + line.substring(pos).length
                    ))
                    break
                }

                // String literal
                if (line[pos] == '"' || line[pos] == '\'') {
                    val quote = line[pos]
                    val start = i + pos
                    pos++
                    while (pos < line.length && line[pos] != quote) {
                        if (line[pos] == '\\') pos++ // skip escaped char
                        pos++
                    }
                    if (pos < line.length) pos++ // skip closing quote
                    tokens.add(SyntaxToken(
                        text = line.substring(start - i, pos),
                        color = AIGreen,
                        start = start,
                        end = i + pos
                    ))
                    continue
                }

                // Number literal
                if (line[pos].isDigit()) {
                    val start = i + pos
                    while (pos < line.length && (line[pos].isDigit() || line[pos] == '.')) {
                        pos++
                    }
                    tokens.add(SyntaxToken(
                        text = line.substring(start - i, pos),
                        color = AIOrange,
                        start = start,
                        end = i + pos
                    ))
                    continue
                }

                // Word (potential keyword)
                if (line[pos].isLetter() || line[pos] == '_') {
                    val start = i + pos
                    while (pos < line.length && (line[pos].isLetterOrDigit() || line[pos] == '_')) {
                        pos++
                    }
                    val word = line.substring(start - i, pos)
                    val color = if (word in keywordsForLanguage) AIPurple else Color.Unspecified
                    tokens.add(SyntaxToken(
                        text = word,
                        color = color,
                        start = start,
                        end = i + pos
                    ))
                    continue
                }

                // Operator or other character
                pos++
            }
            i += line.length + 1 // +1 for newline
        }

        return tokens
    }
}
