package com.nexusai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.nexusai.core.ui.theme.NexusBackground
import com.nexusai.core.ui.theme.NexusPurple
import com.nexusai.core.ui.theme.NexusTextPrimary
import com.nexusai.core.ui.theme.NexusTextSecondary
import com.nexusai.core.ui.theme.NexusTextTertiary

@Composable
fun BiometricLockScreen(
    onAuthenticated: () -> Unit,
    biometricHelper: BiometricHelper,
    activity: FragmentActivity
) {
    LaunchedEffect(Unit) {
        if (biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                activity = activity,
                title = "NexusAI заблокирован",
                subtitle = "Подтвердите личность для входа",
                onSuccess = { onAuthenticated() },
                onError = { },
                onFailed = { }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = NexusPurple.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Приложение заблокировано",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NexusTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Используйте биометрию для входа",
                fontSize = 16.sp,
                color = NexusTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    biometricHelper.authenticate(
                        activity = activity,
                        title = "NexusAI заблокирован",
                        subtitle = "Подтвердите личность для входа",
                        onSuccess = { onAuthenticated() },
                        onError = { },
                        onFailed = { }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexusPurple
                )
            ) {
                Text(
                    text = "Разблокировать",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
