package com.codershubinc.abt.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codershubinc.abt.ui.theme.VoidAccentPrimary
import com.codershubinc.abt.ui.theme.VoidChevronTint
import com.codershubinc.abt.ui.theme.VoidTextMuted
import com.codershubinc.abt.ui.theme.VoidTextWhite

/**
 * Modern Tile Container (Soft Rounded Corners, Borderless Glass Tile)
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF12121A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Modern InfoRow Tile Component
 */
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = false,
    valueColor: Color = VoidTextWhite
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                color = VoidTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = VoidChevronTint
            )
        }
    }
}

/**
 * Modern Toggle Row Tile Component
 */
@Composable
fun NullVoidToggleRow(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) VoidAccentPrimary else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                color = VoidTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = description,
                color = VoidTextWhite,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Modern Pill Switch
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (checked) VoidAccentPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (checked) VoidAccentPrimary else Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

/**
 * Geometric Soundwave Visualizer (Sharp Rectangular Bars)
 */
@Composable
fun SoundWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barColor: Color = VoidAccentPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b5"
    )

    val factors = listOf(bar1, bar2, bar3, bar4, bar5)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val totalBarSpace = width / barCount
        val barWidth = totalBarSpace * 0.5f

        for (i in 0 until barCount) {
            val scale = if (isPlaying) factors[i % factors.size] else 0.15f
            val currentBarHeight = (height * scale).coerceAtLeast(4.dp.toPx())
            val x = i * totalBarSpace + (totalBarSpace - barWidth) / 2
            val y = (height - currentBarHeight) / 2

            drawRect(
                color = if (isPlaying) barColor else Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight)
            )
        }
    }
}

/**
 * Modern Tile Idle Scanner Animation
 */
@Composable
fun NoMusicGeometricAnimation(
    modifier: Modifier = Modifier,
    accentColor: Color = VoidAccentPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nomusic_anim")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "scan_line"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    val tileShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .clip(tileShape)
            .background(Color(0xFF0C0C14)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Geometric Crosshair Lines
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(width / 2, 0f),
                end = Offset(width / 2, height),
                strokeWidth = 1.dp.toPx()
            )

            // Scanning Horizon Beam Line
            val scanY = height * scanLineProgress
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.6f), Color.Transparent),
                    startY = (scanY - 10.dp.toPx()).coerceAtLeast(0f),
                    endY = (scanY + 10.dp.toPx()).coerceAtMost(height)
                ),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IDLE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = accentColor.copy(alpha = pulseAlpha)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "NO MUSIC PLAYING",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = pulseAlpha)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "[ WAITING FOR MEDIA SESSION ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

