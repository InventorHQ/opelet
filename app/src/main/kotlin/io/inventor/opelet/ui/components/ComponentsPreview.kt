package io.inventor.opelet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType

@Preview(showBackground = true, widthDp = 390, name = "Components - Light")
@Composable
private fun ComponentsPreviewLight() {
    OpeletTheme(darkTheme = false) {
        ComponentsShowcase()
    }
}

@Preview(showBackground = true, widthDp = 390, name = "Components - Dark",
    backgroundColor = 0xFF1A1A1A)
@Composable
private fun ComponentsPreviewDark() {
    OpeletTheme(darkTheme = true) {
        ComponentsShowcase()
    }
}

@Composable
private fun ComponentsShowcase() {
    val colors = OpeletTheme.colors

    Column(
        modifier = Modifier
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Typography
        Text("title style", style = OpeletType.title, color = colors.onBackground)
        Text("heading style", style = OpeletType.heading, color = colors.onBackground)
        Text("body style — the quick brown fox", style = OpeletType.body, color = colors.onBackground)
        Text("label style", style = OpeletType.label, color = colors.onBackground)
        Text("caption style", style = OpeletType.caption, color = colors.muted)

        Spacer(Modifier.height(4.dp))

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OpeletButton(text = "install", onClick = {})
            OpeletButton(text = "remove", onClick = {})
            OpeletButton(text = "disabled", onClick = {}, enabled = false)
        }

        // Text field
        OpeletTextField(
            value = "",
            onValueChange = {},
            placeholder = "owner/repo or github url",
        )
        OpeletTextField(
            value = "termux/termux-app",
            onValueChange = {},
        )

        // Loading
        Text("loading:", style = OpeletType.label, color = colors.onBackground)
        LoadingLine()

        // Progress
        Text("progress (60%):", style = OpeletType.label, color = colors.onBackground)
        ProgressLine(progress = 0.6f)

        // Update dots
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("update:", style = OpeletType.label, color = colors.onBackground)
            UpdateDot(hasUpdate = true)
            Text("up to date:", style = OpeletType.label, color = colors.onBackground)
            UpdateDot(hasUpdate = false)
        }

        // Card
        OpeletCard {
            Text("card surface", style = OpeletType.body, color = colors.onSurface)
        }
    }
}
