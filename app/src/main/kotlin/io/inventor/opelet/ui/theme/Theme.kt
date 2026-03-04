package io.inventor.opelet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class OpeletColors(
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val border: Color,
    val muted: Color,
)

private val LightColors = OpeletColors(
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    border = LightBorder,
    muted = Muted,
)

private val DarkColors = OpeletColors(
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    border = DarkBorder,
    muted = Muted,
)

val LocalOpeletColors = staticCompositionLocalOf { LightColors }

object OpeletTheme {
    val colors: OpeletColors
        @Composable get() = LocalOpeletColors.current

    val type: OpeletType = OpeletType
}

@Composable
fun OpeletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalOpeletColors provides colors,
    ) {
        content()
    }
}
