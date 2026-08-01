package com.codershubinc.abt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NullVoidColorScheme = darkColorScheme(
    primary = VoidAccentPrimary,
    onPrimary = VoidTextWhite,
    background = VoidBackground,
    onBackground = VoidTextWhite,
    surface = VoidBackground,
    onSurface = VoidTextWhite,
    outline = VoidDividerColor
)

@Composable
fun ABTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NullVoidColorScheme,
        typography = NullVoidTypography,
        content = content
    )
}