package io.inventor.opelet.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.inventor.opelet.ui.components.OpeletButton
import io.inventor.opelet.ui.components.OpeletCard
import io.inventor.opelet.ui.components.ProgressLine
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Detail - Light")
@Composable
private fun DetailPreviewLight() {
    OpeletTheme(darkTheme = false) {
        DetailPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Detail - Dark",
    backgroundColor = 0xFF1A1A1A)
@Composable
private fun DetailPreviewDark() {
    OpeletTheme(darkTheme = true) {
        DetailPreviewContent()
    }
}

@Composable
private fun DetailPreviewContent() {
    val colors = OpeletTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OpeletButton(text = "< back", onClick = {})
            OpeletButton(text = "remove", onClick = {})
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "termux-app",
            style = OpeletType.title,
            color = colors.onBackground,
        )
        Text(
            text = "termux/termux-app",
            style = OpeletType.caption,
            color = colors.muted,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "installed: v0.118.0",
            style = OpeletType.label,
            color = colors.onBackground,
        )

        Spacer(Modifier.height(8.dp))

        // Simulated download progress
        ProgressLine(progress = 0.6f)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "downloading 60%",
            style = OpeletType.caption,
            color = colors.muted,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "releases",
            style = OpeletType.heading,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                SampleReleaseRow(
                    tag = "v0.119.1",
                    date = "2025-11-15",
                    isInstalled = false,
                    prerelease = false,
                    expanded = true,
                    notes = "## What's new\n- Fixed crash on Android 15\n- Improved terminal rendering performance\n- Updated bootstrap packages",
                )
            }
            item {
                SampleReleaseRow(
                    tag = "v0.119.0",
                    date = "2025-10-28",
                    isInstalled = false,
                    prerelease = false,
                    expanded = false,
                    notes = "",
                )
            }
            item {
                SampleReleaseRow(
                    tag = "v0.119.0-rc1",
                    date = "2025-10-20",
                    isInstalled = false,
                    prerelease = true,
                    expanded = false,
                    notes = "",
                )
            }
            item {
                SampleReleaseRow(
                    tag = "v0.118.0",
                    date = "2025-09-12",
                    isInstalled = true,
                    prerelease = false,
                    expanded = false,
                    notes = "",
                )
            }
        }
    }
}

@Composable
private fun SampleReleaseRow(
    tag: String,
    date: String,
    isInstalled: Boolean,
    prerelease: Boolean,
    expanded: Boolean,
    notes: String,
) {
    val colors = OpeletTheme.colors

    OpeletCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tag,
                            style = OpeletType.heading,
                            color = colors.onSurface,
                        )
                        if (prerelease) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "pre",
                                style = OpeletType.caption,
                                color = colors.muted,
                            )
                        }
                        if (isInstalled) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "installed",
                                style = OpeletType.caption,
                                color = colors.onSurface,
                            )
                        }
                    }
                    Text(
                        text = date,
                        style = OpeletType.caption,
                        color = colors.muted,
                    )
                }

                Row {
                    OpeletButton(text = "pin", onClick = {})
                    Spacer(Modifier.width(8.dp))
                    OpeletButton(text = "install", onClick = {})
                }
            }

            if (expanded && notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                ) {
                    Text(
                        text = notes,
                        style = OpeletType.caption,
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}
