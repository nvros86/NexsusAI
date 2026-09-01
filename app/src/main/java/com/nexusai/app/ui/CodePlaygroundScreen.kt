package com.nexusai.app.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusCard
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusSurface
import com.nexusai.core.ui.theme.NexusSurfaceVariant
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

private const val DEFAULT_HTML = """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>NexusAI Playground</title>
</head>
<body>
  <div class="container">
    <h1>👋 Добро пожаловать!</h1>
    <p>Редактируй HTML, CSS и JS — и смотри результат в реальном времени.</p>
    <button onclick="greet()">Нажми меня</button>
    <div id="output"></div>
  </div>
</body>
</html>"""

private const val DEFAULT_CSS = """* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Segoe UI', Tahoma, sans-serif;
  background: #1a1a2e;
  color: #e0e0e0;
  padding: 20px;
}
.container {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}
h1 {
  color: #a855f7;
  margin-bottom: 12px;
}
p {
  color: #9ca3af;
  margin-bottom: 20px;
}
button {
  background: #a855f7;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}
button:hover {
  background: #9333ea;
}
#output {
  margin-top: 16px;
  padding: 12px;
  background: #16213e;
  border-radius: 8px;
  min-height: 40px;
}"""

private const val DEFAULT_JS = """function greet() {
  const output = document.getElementById('output');
  output.innerHTML = '<p style="color: #a855f7; font-size: 18px;">🚀 Привет от NexusAI Playground!</p>';
  output.style.animation = 'fadeIn 0.3s ease';
}"""

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodePlaygroundScreen(
    onBack: () -> Unit = {}
) {
    var html by remember { mutableStateOf(DEFAULT_HTML) }
    var css by remember { mutableStateOf(DEFAULT_CSS) }
    var js by remember { mutableStateOf(DEFAULT_JS) }
    var activeTab by remember { mutableIntStateOf(0) }
    var previewHtml by remember { mutableStateOf(buildPreview(html, css, js)) }

    LaunchedEffect(html, css, js) {
        delay(500)
        previewHtml = buildPreview(html, css, js)
    }

    val tabs = listOf("HTML", "CSS", "JS")

    Scaffold(
        containerColor = NexusBackground,
        topBar = {
            TopAppBar(
                title = { Text("Code Playground", color = NexusTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = NexusTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { previewHtml = buildPreview(html, css, js) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = NexusPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, label ->
                    TabChip(
                        label = label,
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        modifier = Modifier.weight(1f)
                    )
                    if (index < tabs.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexusSurface)
            ) {
                when (activeTab) {
                    0 -> CodeEditorArea(code = html, onCodeChange = { html = it }, label = "HTML")
                    1 -> CodeEditorArea(code = css, onCodeChange = { css = it }, label = "CSS")
                    2 -> CodeEditorArea(code = js, onCodeChange = { js = it }, label = "JavaScript")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = NexusPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            loadDataWithBaseURL(
                                null,
                                previewHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            null,
                            previewHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NexusPurple else NexusSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) NexusTextPrimary else NexusTextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CodeEditorArea(code: String, onCodeChange: (String) -> Unit, label: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = NexusTextTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NexusTextTertiary
            )
        }
        androidx.compose.foundation.text.BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = NexusTextPrimary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(NexusPurple)
        )
    }
}

private fun buildPreview(html: String, css: String, js: String): String {
    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>$css</style>
</head>
<body>
$html
<script>$js</script>
</body>
</html>
    """.trimIndent()
}
