package io.inventor.opelet.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.inventor.opelet.ui.components.LoadingLine
import io.inventor.opelet.ui.components.OpeletButton
import io.inventor.opelet.ui.components.OpeletCard
import io.inventor.opelet.ui.components.OpeletTextField
import io.inventor.opelet.ui.components.UpdateDot
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "App List - Light")
@Composable
private fun AppListPreviewLight() {
    OpeletTheme(darkTheme = false) {
        AppListPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "App List - Dark",
    backgroundColor = 0xFF1A1A1A)
@Composable
private fun AppListPreviewDark() {
    OpeletTheme(darkTheme = true) {
        AppListPreviewContent()
    }
}

@Composable
private fun AppListPreviewContent() {
    val colors = OpeletTheme.colors

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "opelet",
                    style = OpeletType.title,
                    color = colors.onBackground,
                )
                OpeletButton(text = "settings", onClick = {})
            }
        }

        // Add input
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OpeletTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "owner/repo or github url",
                    )
                }
                Spacer(Modifier.width(8.dp))
                OpeletButton(text = "add", onClick = {})
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Sample apps
        item {
            SampleAppRow(
                name = "opelet",
                owner = "InventorHQ",
                installed = "v0.1.0",
                latest = "v0.1.0",
                hasUpdate = false,
                isSelf = true,
            )
        }
        item {
            SampleAppRow(
                name = "termux-app",
                owner = "termux",
                installed = "v0.118.0",
                latest = "v0.119.1",
                hasUpdate = true,
                isSelf = false,
            )
        }
        item {
            SampleAppRow(
                name = "Seal",
                owner = "JunkFood02",
                installed = "v1.12.1",
                latest = "v1.12.1",
                hasUpdate = false,
                isSelf = false,
            )
        }
        item {
            SampleAppRow(
                name = "syncthing-android",
                owner = "syncthing",
                installed = null,
                latest = "v1.27.0",
                hasUpdate = false,
                isSelf = false,
            )
        }
    }
}

@Composable
private fun SampleAppRow(
    name: String,
    owner: String,
    installed: String?,
    latest: String,
    hasUpdate: Boolean,
    isSelf: Boolean,
) {
    val colors = OpeletTheme.colors

    OpeletCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateDot(hasUpdate = hasUpdate)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name + if (isSelf) " (self)" else "",
                    style = OpeletType.heading,
                    color = colors.onSurface,
                )
                Text(
                    text = owner,
                    style = OpeletType.caption,
                    color = colors.muted,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = installed ?: "—",
                    style = OpeletType.label,
                    color = colors.onSurface,
                )
                if (installed != latest) {
                    Text(
                        text = latest,
                        style = OpeletType.caption,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}
