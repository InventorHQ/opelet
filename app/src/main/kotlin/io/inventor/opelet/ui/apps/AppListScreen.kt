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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.inventor.opelet.data.TrackedApp
import io.inventor.opelet.data.updateStatus
import io.inventor.opelet.model.UpdateStatus
import io.inventor.opelet.ui.components.LoadingLine
import io.inventor.opelet.ui.components.OpeletButton
import io.inventor.opelet.ui.components.OpeletCard
import io.inventor.opelet.ui.components.OpeletTextField
import io.inventor.opelet.ui.components.UpdateDot
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onAppClick: (String) -> Unit, // repoFullName
    onSettingsClick: () -> Unit,
    viewModel: AppListViewModel = viewModel(),
) {
    val apps by viewModel.apps.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val addInput by viewModel.addInput.collectAsState()
    val addError by viewModel.addError.collectAsState()
    val isAdding by viewModel.isAdding.collectAsState()
    val colors = OpeletTheme.colors

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
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
                    OpeletButton(
                        text = "settings",
                        onClick = onSettingsClick,
                    )
                }
            }

            // Add repo input — inline, not a dialog
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OpeletTextField(
                                value = addInput,
                                onValueChange = viewModel::onAddInputChanged,
                                placeholder = "owner/repo or github url",
                                onSubmit = viewModel::addApp,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        OpeletButton(
                            text = "add",
                            onClick = viewModel::addApp,
                            enabled = !isAdding,
                        )
                    }
                    if (isAdding) {
                        Spacer(Modifier.height(4.dp))
                        LoadingLine()
                    }
                    addError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            style = OpeletType.caption,
                            color = colors.muted,
                        )
                    }
                }
            }

            // Spacer
            item { Spacer(Modifier.height(8.dp)) }

            // App list
            if (apps.isEmpty() && !isRefreshing) {
                item {
                    Text(
                        text = "no apps tracked yet",
                        style = OpeletType.body,
                        color = colors.muted,
                        modifier = Modifier.padding(vertical = 32.dp),
                    )
                }
            }

            items(apps, key = { it.repoFullName }) { app ->
                AppRow(
                    app = app,
                    onClick = { onAppClick(app.repoFullName) },
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: TrackedApp,
    onClick: () -> Unit,
) {
    val colors = OpeletTheme.colors
    val status = app.updateStatus()

    OpeletCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateDot(
                hasUpdate = status == UpdateStatus.UPDATE_AVAILABLE,
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.repo + if (app.isSelf) " (self)" else "",
                    style = OpeletType.heading,
                    color = colors.onSurface,
                )
                Text(
                    text = app.owner,
                    style = OpeletType.caption,
                    color = colors.muted,
                )
                if (!app.description.isNullOrBlank()) {
                    Text(
                        text = app.description,
                        style = OpeletType.caption,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = app.installedVersion ?: "—",
                    style = OpeletType.label,
                    color = colors.onSurface,
                )
                val target = app.latestStableVersion ?: app.latestVersion
                if (target != null && target != app.installedVersion) {
                    Text(
                        text = target,
                        style = OpeletType.caption,
                        color = colors.muted,
                    )
                }
                if (status == UpdateStatus.PINNED) {
                    Text(
                        text = "pinned",
                        style = OpeletType.caption,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}
