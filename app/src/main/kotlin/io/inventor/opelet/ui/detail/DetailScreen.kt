package io.inventor.opelet.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import io.inventor.opelet.model.GitHubAsset
import io.inventor.opelet.model.GitHubRelease
import io.inventor.opelet.ui.components.LoadingLine
import io.inventor.opelet.ui.components.OpeletButton
import io.inventor.opelet.ui.components.OpeletCard
import io.inventor.opelet.ui.components.ProgressLine
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(),
) {
    val app by viewModel.app.collectAsState()
    val releases by viewModel.releases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val downloadState by viewModel.downloadProgress.collectAsState()
    val apkPickerAssets by viewModel.apkPicker.collectAsState()
    val colors = OpeletTheme.colors

    // APK picker dialog
    apkPickerAssets?.let { assets ->
        ApkPickerDialog(
            assets = assets,
            onPick = viewModel::pickApk,
            onDismiss = viewModel::dismissPicker,
        )
    }

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
            OpeletButton(text = "< back", onClick = onBack)

            app?.let { appData ->
                if (!appData.isSelf) {
                    OpeletButton(
                        text = "remove",
                        onClick = {
                            viewModel.removeApp()
                            onBack()
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // App info
        app?.let { appData ->
            Text(
                text = appData.repo,
                style = OpeletType.title,
                color = colors.onBackground,
            )
            Text(
                text = appData.owner + "/" + appData.repo,
                style = OpeletType.caption,
                color = colors.muted,
            )
            if (!appData.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = appData.description,
                    style = OpeletType.body,
                    color = colors.onBackground,
                )
            }
            Spacer(Modifier.height(4.dp))

            Row {
                Text(
                    text = "installed: ${appData.installedVersion ?: "none"}",
                    style = OpeletType.label,
                    color = colors.onBackground,
                )
                Spacer(Modifier.width(16.dp))
                if (appData.pinnedVersion != null) {
                    Text(
                        text = "pinned: ${appData.pinnedVersion}",
                        style = OpeletType.label,
                        color = colors.muted,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Download progress
        when (val state = downloadState) {
            is DownloadState.Downloading -> {
                ProgressLine(progress = state.progress)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "downloading ${(state.progress * 100).toInt()}%",
                    style = OpeletType.caption,
                    color = colors.muted,
                )
                Spacer(Modifier.height(8.dp))
            }
            DownloadState.Idle -> {}
        }

        // Error
        error?.let { msg ->
            Text(
                text = msg,
                style = OpeletType.caption,
                color = colors.muted,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Loading
        if (isLoading) {
            LoadingLine()
            Spacer(Modifier.height(8.dp))
        }

        // Pin/unpin
        app?.let { appData ->
            Row {
                if (appData.pinnedVersion != null) {
                    OpeletButton(
                        text = "unpin",
                        onClick = { viewModel.pinVersion(null) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Releases
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
            items(releases, key = { it.id }) { release ->
                ReleaseRow(
                    release = release,
                    isInstalled = app?.installedVersion == release.tagName,
                    onInstall = { viewModel.installRelease(release) },
                    onPin = { viewModel.pinVersion(release.tagName) },
                )
            }
        }
    }
}

@Composable
private fun ReleaseRow(
    release: GitHubRelease,
    isInstalled: Boolean,
    onInstall: () -> Unit,
    onPin: () -> Unit,
) {
    val colors = OpeletTheme.colors
    var expanded by remember { mutableStateOf(false) }

    OpeletCard(onClick = { expanded = !expanded }) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = release.tagName,
                            style = OpeletType.heading,
                            color = colors.onSurface,
                        )
                        if (release.prerelease) {
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
                    if (release.publishedAt.isNotEmpty()) {
                        Text(
                            text = release.publishedAt.take(10), // just the date
                            style = OpeletType.caption,
                            color = colors.muted,
                        )
                    }
                }

                Row {
                    OpeletButton(text = "pin", onClick = onPin)
                    Spacer(Modifier.width(8.dp))
                    OpeletButton(text = "install", onClick = onInstall)
                }
            }

            // Expanded: show release notes
            if (expanded && release.body.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                ) {
                    Text(
                        text = release.body,
                        style = OpeletType.caption,
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApkPickerDialog(
    assets: List<GitHubAsset>,
    onPick: (GitHubAsset) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = OpeletTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(colors.background, RoundedCornerShape(4.dp))
                .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                .padding(16.dp),
        ) {
            Text(
                text = "select APK",
                style = OpeletType.heading,
                color = colors.onBackground,
            )
            Spacer(Modifier.height(12.dp))

            assets.forEach { asset ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPick(asset) }
                        .padding(12.dp),
                ) {
                    Column {
                        Text(
                            text = asset.name,
                            style = OpeletType.body,
                            color = colors.onBackground,
                        )
                        Text(
                            text = formatSize(asset.size),
                            style = OpeletType.caption,
                            color = colors.muted,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            OpeletButton(text = "cancel", onClick = onDismiss)
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
