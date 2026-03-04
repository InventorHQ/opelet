package io.inventor.opelet.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.inventor.opelet.ui.theme.OpeletTheme
import io.inventor.opelet.ui.theme.OpeletType
import androidx.compose.material3.Text

private val SmallRadius = RoundedCornerShape(4.dp)

/**
 * Outlined button — the only button style in opelet.
 * Thin border, monospace text, no fill.
 */
@Composable
fun OpeletButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = OpeletTheme.colors
    val borderColor = if (enabled) colors.border else colors.muted
    val textColor = if (enabled) colors.onBackground else colors.muted

    Box(
        modifier = modifier
            .border(1.dp, borderColor, SmallRadius)
            .clip(SmallRadius)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = OpeletType.button,
            color = textColor,
        )
    }
}

/**
 * Text input field — thin border, monospace, no decoration.
 */
@Composable
fun OpeletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onSubmit: () -> Unit = {},
) {
    val colors = OpeletTheme.colors

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = OpeletType.body.copy(color = colors.onBackground),
        cursorBrush = SolidColor(colors.onBackground),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        decorationBox = { innerTextField ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, SmallRadius)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = OpeletType.body,
                        color = colors.muted,
                    )
                }
                innerTextField()
            }
        },
    )
}

/**
 * Thin indeterminate loading line. No spinners, per spec.
 */
@Composable
fun LoadingLine(modifier: Modifier = Modifier) {
    val colors = OpeletTheme.colors
    val transition = rememberInfiniteTransition(label = "loading")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loading-offset",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(2.dp)
                .align(Alignment.CenterStart)
                .padding(start = (offset * 250).dp) // slides across
                .background(colors.onBackground),
        )
    }
}

/**
 * A surface card — thin border, flat fill.
 */
@Composable
fun OpeletCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = OpeletTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, SmallRadius)
            .clip(SmallRadius)
            .background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
    ) {
        content()
    }
}

/**
 * Update indicator — filled circle = update available, outline = up to date.
 * Uses stroke weight, not color.
 */
@Composable
fun UpdateDot(hasUpdate: Boolean, modifier: Modifier = Modifier) {
    val colors = OpeletTheme.colors
    val size = 8.dp

    if (hasUpdate) {
        Box(
            modifier = modifier
                .width(size)
                .height(size)
                .clip(RoundedCornerShape(50))
                .background(colors.onBackground),
        )
    } else {
        Box(
            modifier = modifier
                .width(size)
                .height(size)
                .border(1.dp, colors.border, RoundedCornerShape(50)),
        )
    }
}

/**
 * Download progress bar — thin line showing percentage.
 */
@Composable
fun ProgressLine(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
) {
    val colors = OpeletTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(colors.onBackground),
        )
    }
}
