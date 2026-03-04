package io.inventor.opelet.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.inventor.opelet.ui.components.OpeletButton
import io.inventor.opelet.ui.components.OpeletCard
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType
import io.inventor.opelet.worker.UpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val colors = OpeletTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cacheCleared by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf(calculateCacheSize(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OpeletButton(text = "< back", onClick = onBack)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "settings",
            style = OpeletType.title,
            color = colors.onBackground,
        )

        Spacer(Modifier.height(16.dp))

        // Background check interval
        OpeletCard {
            Column {
                Text(
                    text = "background checks",
                    style = OpeletType.heading,
                    color = colors.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "checks for updates every 6 hours",
                    style = OpeletType.caption,
                    color = colors.muted,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Cache management
        OpeletCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "downloaded APKs",
                        style = OpeletType.heading,
                        color = colors.onSurface,
                    )
                    Text(
                        text = if (cacheCleared) "cleared" else cacheSize,
                        style = OpeletType.caption,
                        color = colors.muted,
                    )
                }
                OpeletButton(
                    text = "clear",
                    onClick = {
                        scope.launch {
                            clearCache(context)
                            cacheCleared = true
                            cacheSize = "0 B"
                        }
                    },
                    enabled = !cacheCleared,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // About
        OpeletCard {
            Column {
                Text(
                    text = "about",
                    style = OpeletType.heading,
                    color = colors.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "opelet — app zero",
                    style = OpeletType.body,
                    color = colors.onSurface,
                )
                Text(
                    text = "github.com/InventorHQ/opelet",
                    style = OpeletType.caption,
                    color = colors.muted,
                )
            }
        }
    }
}

private fun calculateCacheSize(context: Context): String {
    val apkDir = File(context.filesDir, "apks")
    if (!apkDir.exists()) return "0 B"
    val totalBytes = apkDir.listFiles()?.sumOf { it.length() } ?: 0L
    return formatBytes(totalBytes)
}

private suspend fun clearCache(context: Context) {
    withContext(Dispatchers.IO) {
        val apkDir = File(context.filesDir, "apks")
        apkDir.listFiles()?.forEach { it.delete() }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
