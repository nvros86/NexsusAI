package com.nexsusai.workstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexsusAIWorkstation()
        }
    }
}

@Composable
fun NexsusAIWorkstation() {
    var tabs by remember { mutableStateOf(listOf("AI Session 1")) }
    var selected by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("NexsusAI Workstation", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            tabs.forEachIndexed { index, title ->
                Button(onClick = { selected = index }) {
                    Text(title)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(onClick = {
                tabs = tabs + "AI Session ${tabs.size + 1}"
            }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Active workspace: ${tabs[selected]}")
        Text("Independent AI context will be connected in AI Engine module")
    }
}
