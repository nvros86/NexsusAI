package com.nexusai.app.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusai.core.ui.components.AIProviderIcon
import com.nexusai.core.ui.components.ProviderBadge
import com.nexusai.core.ui.components.file.FileHelper
import com.nexusai.core.ui.theme.AIBlue
import com.nexusai.core.ui.theme.AIGreen
import com.nexusai.core.ui.theme.AIOrange
import com.nexusai.core.ui.theme.AIPurple
import com.nexusai.core.ui.theme.NexsusAITheme
import com.nexusai.core.ui.theme.Purple40
import com.nexusai.core.ui.theme.Purple80
import com.nexusai.domain.model.AIProviderConfig
import com.nexusai.domain.model.Message
import com.nexusai.domain.model.MessageRole
import com.nexusai.domain.model.ProviderType

// ============================================================
// 1. THEME & COLORS
// ============================================================

@Preview(name = "01 - Color Palette Light", showBackground = true, widthDp = 400)
@Composable
fun PreviewColorPaletteLight() {
    NexsusAITheme(darkTheme = false) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("NexsusAI Color Palette", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Primary Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("Purple80", Purple80)
                ColorSwatch("Purple40", Purple40)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("AI Accent Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("Blue", AIBlue)
                ColorSwatch("Green", AIGreen)
                ColorSwatch("Purple", AIPurple)
                ColorSwatch("Orange", AIOrange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Provider Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("OpenAI", Color(0xFF10A37F))
                ColorSwatch("Anthropic", Color(0xFFD97706))
                ColorSwatch("Google", Color(0xFF4285F4))
                ColorSwatch("Copilot", Color(0xFF8B5CF6))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("Success", Color(0xFF22C55E))
                ColorSwatch("Warning", Color(0xFFF59E0B))
                ColorSwatch("Error", Color(0xFFEF4444))
                ColorSwatch("Info", Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(color)
        )
        Text(name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}

@Preview(name = "01b - Color Palette Dark", showBackground = true, widthDp = 400)
@Composable
fun PreviewColorPaletteDark() {
    NexsusAITheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("NexsusAI Color Palette (Dark)", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Surface Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
                ColorSwatch("SurfaceVar", MaterialTheme.colorScheme.surfaceVariant)
                ColorSwatch("Card", MaterialTheme.colorScheme.surfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Message Colors", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch("User", MaterialTheme.colorScheme.primaryContainer)
                ColorSwatch("Assistant", MaterialTheme.colorScheme.surfaceVariant)
                ColorSwatch("System", Color(0xFF1B3A1B))
            }
        }
    }
}

// ============================================================
// 2. TYPOGRAPHY
// ============================================================

@Preview(name = "02 - Typography", showBackground = true, widthDp = 400)
@Composable
fun PreviewTypography() {
    NexsusAITheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Typography Scale", style = MaterialTheme.typography.headlineMedium)
            HorizontalDivider()
            Text("Display Large", style = MaterialTheme.typography.displayLarge)
            Text("Display Medium", style = MaterialTheme.typography.displayMedium)
            Text("Display Small", style = MaterialTheme.typography.displaySmall)
            HorizontalDivider()
            Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
            Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
            Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider()
            Text("Title Large", style = MaterialTheme.typography.titleLarge)
            Text("Title Medium", style = MaterialTheme.typography.titleMedium)
            Text("Title Small", style = MaterialTheme.typography.titleSmall)
            HorizontalDivider()
            Text("Body Large", style = MaterialTheme.typography.bodyLarge)
            Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
            Text("Body Small", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text("Label Large", style = MaterialTheme.typography.labelLarge)
            Text("Label Medium", style = MaterialTheme.typography.labelMedium)
            Text("Label Small", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ============================================================
// 3. CHAT SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "03 - Chat Screen", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewChatScreen() {
    val messages = listOf(
        Message(id = "1", content = "Hello! How can I help you today?", role = MessageRole.ASSISTANT),
        Message(id = "2", content = "Can you explain how Jetpack Compose works?", role = MessageRole.USER),
        Message(id = "3", content = "Jetpack Compose is Android's modern toolkit for building native UI. It uses a declarative approach where you describe your UI in composable functions.\n\nKey concepts:\n• Composables: Functions annotated with @Composable\n• State: Reactive data that triggers recomposition\n• Recomposition: When state changes, only affected composables are redrawn\n\nThis makes UI code more concise and easier to maintain.", role = MessageRole.ASSISTANT),
        Message(id = "4", content = "That's great! Can you show me a simple example?", role = MessageRole.USER),
        Message(id = "5", content = "Sure! Here's a simple counter example:\n\n@Composable\nfun Counter() {\n    var count by remember { mutableStateOf(0) }\n    Button(onClick = { count++ }) {\n        Text(\"Count: \$count\")\n    }\n}", role = MessageRole.ASSISTANT)
    )

    NexsusAITheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(title = { Text("NexsusAI") }, actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.SwapHoriz, "Switch provider") }
                })
                TabBarPreview()
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message -> MessageBubblePreview(message = message) }
                }
                MessageInputPreview()
            }
        }
    }
}

@Composable
private fun MessageBubblePreview(message: Message) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp).widthIn(max = 300.dp),
            shape = RoundedCornerShape(topStart = if (isUser) 16.dp else 4.dp, topEnd = if (isUser) 4.dp else 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(text = message.content, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isUser) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer).padding(6.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

// ============================================================
// 4. TAB BAR
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "04 - Tab Bar", showBackground = true, widthDp = 400)
@Composable
fun TabBarPreview() {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TabItem("Chat 1", isActive = true)
            TabItem("Code Review", isActive = false)
            TabItem("Brainstorm", isActive = false)
            IconButton(onClick = {}, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer)) {
                Icon(Icons.Default.Add, "New tab", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TabItem(title: String, isActive: Boolean) {
    Row(modifier = Modifier.height(36.dp).clip(RoundedCornerShape(8.dp))
        .background(if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelMedium,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(Icons.Default.Close, "Close", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================
// 5. MESSAGE INPUT
// ============================================================

@Preview(name = "05 - Message Input", showBackground = true, widthDp = 400)
@Composable
fun MessageInputPreview() {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Image, "Image", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.AttachFile, "File", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                OutlinedTextField(value = "Type a message...", onValueChange = {}, modifier = Modifier.weight(1f), maxLines = 1,
                    shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), focusedBorderColor = MaterialTheme.colorScheme.primary))
                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Mic, "Voice", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

// ============================================================
// 6. SETTINGS SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "06 - Settings Screen", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewSettingsScreen() {
    val providers = listOf(
        AIProviderConfig(id = "1", name = "OpenAI", type = ProviderType.OPENAI, baseUrl = "https://api.openai.com/v1", defaultModel = "gpt-4o", isFavorite = true),
        AIProviderConfig(id = "2", name = "Claude", type = ProviderType.ANTHROPIC, baseUrl = "https://api.anthropic.com/v1", defaultModel = "claude-3-5-sonnet-20241022"),
        AIProviderConfig(id = "3", name = "Gemini", type = ProviderType.GEMINI, baseUrl = "https://generativelanguage.googleapis.com", defaultModel = "gemini-pro")
    )
    NexsusAITheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) },
            floatingActionButton = {
                Surface(onClick = {}, modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Add Provider") }
                }
            }) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("AI Providers", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                items(providers) { provider -> ProviderCardPreview(provider) }
            }
        }
    }
}

@Composable
private fun ProviderCardPreview(provider: AIProviderConfig) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AIProviderIcon(providerId = provider.type.name)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, style = MaterialTheme.typography.titleMedium)
                Text(provider.type.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Model: ${provider.defaultModel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Star, "Favorite", tint = if (provider.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = {}) { Icon(Icons.Default.Edit, "Edit") }
            IconButton(onClick = {}) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ============================================================
// 7. EDITOR SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "07 - Editor Screen", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewEditorScreen() {
    var count = 0
    val sampleCode = """package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelloWorld() {
    var count by remember {
        mutableStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hello, World!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = { count++ }
        ) {
            Text("Clicks: \$count")
        }
    }
}"""

    NexsusAITheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(title = { Text("HelloWorld.kt") }, navigationIcon = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Close, "Close") }
                }, actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Image, "Open") }
                    IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.Send, "Save") }
                })
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.width(48.dp).background(MaterialTheme.colorScheme.surface).padding(end = 8.dp, top = 8.dp)) {
                        Text(text = (1..sampleCode.lines().size).joinToString("\n"), fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 20.sp, modifier = Modifier.padding(end = 8.dp))
                    }
                    Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                        Text(text = sampleCode, fontFamily = FontFamily.Monospace, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                    }
                }
                Text(text = "Ln 28, Col 1 | KOTLIN | 28 lines", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}

// ============================================================
// 8. EMPTY STATE
// ============================================================

@Preview(name = "08 - Empty Tab State", showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun PreviewEmptyTabState() {
    NexsusAITheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("NexsusAI", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("Start a conversation", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilledTonalButton(onClick = {}) { Text("Create new tab") }
            }
        }
    }
}

// ============================================================
// 9. PROVIDER SELECTOR DIALOG
// ============================================================

@Preview(name = "09 - Provider Selector", showBackground = true, widthDp = 400)
@Composable
fun PreviewProviderSelectorDialog() {
    val providers = listOf(
        AIProviderConfig(id = "1", name = "OpenAI", type = ProviderType.OPENAI, baseUrl = "", defaultModel = "gpt-4o"),
        AIProviderConfig(id = "2", name = "Claude", type = ProviderType.ANTHROPIC, baseUrl = "", defaultModel = "claude-3-5-sonnet"),
        AIProviderConfig(id = "3", name = "Gemini", type = ProviderType.GEMINI, baseUrl = "", defaultModel = "gemini-pro")
    )
    NexsusAITheme {
        AlertDialog(onDismissRequest = {}, title = { Text("Select AI Provider") }, text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(providers) { provider ->
                    val isSelected = provider.id == "1"
                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider.name, style = MaterialTheme.typography.bodyLarge)
                                Text(provider.type.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) { Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = {}) { Text("Close") } })
    }
}

// ============================================================
// 10. FILE ATTACHMENTS
// ============================================================

@Preview(name = "10 - File Attachments", showBackground = true, widthDp = 400)
@Composable
fun PreviewFileAttachments() {
    NexsusAITheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("File Attachments", style = MaterialTheme.typography.headlineSmall)
            FileAttachmentRow("screenshot.png", 2_450_000L, Icons.Default.Image)
            FileAttachmentRow("notes.txt", 1_200L, Icons.Default.Description)
            FileAttachmentRow("MainActivity.kt", 4_500L, Icons.Default.InsertDriveFile)
            FileAttachmentRow("report.pdf", 15_000_000L, Icons.Default.InsertDriveFile)
        }
    }
}

@Composable
private fun FileAttachmentRow(name: String, size: Long, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(AIBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = AIBlue)
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(FileHelper.formatFileSize(size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

// ============================================================
// 11. PROVIDER BADGES
// ============================================================

@Preview(name = "11 - Provider Badges", showBackground = true, widthDp = 400)
@Composable
fun PreviewProviderBadges() {
    NexsusAITheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Provider Badges", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProviderBadge("OpenAI")
                ProviderBadge("Anthropic")
                ProviderBadge("Gemini")
            }
        }
    }
}

// ============================================================
// 12. MAIN APP SHELL
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "12 - Main App Shell", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewMainAppShell() {
    NexsusAITheme {
        Scaffold(bottomBar = {
            NavigationBar {
                val items = listOf("Home" to Icons.Default.Image, "Tabs" to Icons.Default.SmartToy, "AI Provider" to Icons.Default.SwapHoriz, "Settings" to Icons.Default.Star)
                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(icon = { Icon(icon, label) }, label = { Text(label) }, selected = index == 1, onClick = {})
                }
            }
        }) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TopAppBar(title = { Text("NexsusAI") }, actions = { IconButton(onClick = {}) { Icon(Icons.Default.SwapHoriz, "Provider") } })
                TabBarPreview()
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NexsusAI", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Select a tab to start", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ============================================================
// 13. GENERATING STATE
// ============================================================

@Preview(name = "13 - Generating State", showBackground = true, widthDp = 400)
@Composable
fun PreviewGeneratingState() {
    NexsusAITheme {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============================================================
// 14. ADD/EDIT PROVIDER DIALOG
// ============================================================

@Preview(name = "14 - Add Provider Dialog", showBackground = true, widthDp = 400)
@Composable
fun PreviewAddProviderDialog() {
    NexsusAITheme {
        AlertDialog(onDismissRequest = {}, title = { Text("Add Provider") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "OPENAI", onValueChange = {}, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "https://api.openai.com/v1", onValueChange = {}, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "gpt-4o", onValueChange = {}, label = { Text("Default Model") }, modifier = Modifier.fillMaxWidth())
                Text("Max Tokens: 4096", style = MaterialTheme.typography.bodyMedium)
                Text("Temperature: 0.7", style = MaterialTheme.typography.bodyMedium)
            }
        }, confirmButton = { TextButton(onClick = {}, enabled = false) { Text("Save") } },
            dismissButton = { TextButton(onClick = {}) { Text("Cancel") } })
    }
}

// ============================================================
// 15. RENAME DIALOG
// ============================================================

@Preview(name = "15 - Rename Tab Dialog", showBackground = true, widthDp = 400)
@Composable
fun PreviewRenameDialog() {
    NexsusAITheme {
        AlertDialog(onDismissRequest = {}, title = { Text("Rename Tab") }, text = {
            OutlinedTextField(value = "Chat 1", onValueChange = {}, label = { Text("Tab name") }, modifier = Modifier.fillMaxWidth())
        }, confirmButton = { TextButton(onClick = {}) { Text("Rename") } },
            dismissButton = { TextButton(onClick = {}) { Text("Cancel") } })
    }
}
