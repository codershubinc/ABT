package com.codershubinc.abt.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.codershubinc.abt.data.MediaState
import com.codershubinc.abt.ui.components.ModernCard
import com.codershubinc.abt.ui.components.NoMusicGeometricAnimation
import com.codershubinc.abt.ui.components.SoundWaveVisualizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun MusicWidgetScreen(
    mediaState: MediaState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTabletOrWidescreen = configuration.screenWidthDp >= 600 || configuration.screenWidthDp > configuration.screenHeightDp

    // Dynamic Color Extraction via Palette API
    var primaryAccent by remember { mutableStateOf(Color(0xFF3D5AFE)) }
    var secondaryAccent by remember { mutableStateOf(Color(0xFF00F0FF)) }

    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    LaunchedEffect(mediaState.artworkBitmap) {
        val bitmap = mediaState.artworkBitmap
        if (bitmap != null) {
            withContext(Dispatchers.Default) {
                try {
                    val palette = Palette.from(bitmap).generate()
                    val dom = palette.getVibrantColor(palette.getDominantColor(0xFF3D5AFE.toInt()))
                    val sec = palette.getLightVibrantColor(palette.getMutedColor(0xFF00F0FF.toInt()))
                    primaryAccent = Color(dom)
                    secondaryAccent = Color(sec)
                } catch (e: Exception) {
                    primaryAccent = Color(0xFF3D5AFE)
                    secondaryAccent = Color(0xFF00F0FF)
                }
            }
        } else {
            primaryAccent = Color(0xFF3D5AFE)
            secondaryAccent = Color(0xFF00F0FF)
        }
    }

    val animatedPrimaryAccent by animateColorAsState(targetValue = primaryAccent, animationSpec = tween(1000), label = "prim_anim")
    val animatedSecondaryAccent by animateColorAsState(targetValue = secondaryAccent, animationSpec = tween(1000), label = "sec_anim")

    val isMusicActive = mediaState.hasActiveSession && (mediaState.isPlaying || mediaState.title != null)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040406))
    ) {
        // Dot Grid Pattern Overlay
        DotGridBackground(modifier = Modifier.fillMaxSize())

        // Top-Left Neon Ambient Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopStart)
                .offset(x = (-120).dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(animatedPrimaryAccent.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        // Heavily Blurred Dynamic Album Backdrop
        if (mediaState.artworkBitmap != null && isMusicActive) {
            Image(
                bitmap = mediaState.artworkBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(90.dp)
                    .background(Color.Black.copy(alpha = 0.65f))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            )
        }

        // Full Screen Structural Geometric Layout (No Curves, Zero Scroll)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = if (isTabletOrWidescreen) 32.dp else 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Left = Track Title / Artist / Album Metadata; Right = Sound Quality Stack
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top-Left Metadata: Massive Track Title, Artist, Album ("no data" if missing)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoundWaveVisualizer(
                            isPlaying = mediaState.isPlaying,
                            modifier = Modifier
                                .width(22.dp)
                                .height(16.dp),
                            barColor = animatedPrimaryAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ABT // NULL VOID GADGET",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Massive Track Title Scaling
                    Text(
                        text = if (isMusicActive) mediaState.displayTitle.uppercase() else "NO MUSIC PLAYING",
                        color = Color.White,
                        fontSize = if (isTabletOrWidescreen) 34.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.shadow(12.dp, RectangleShape, spotColor = animatedPrimaryAccent)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Artist Name
                    Text(
                        text = if (isMusicActive) mediaState.displayArtist.uppercase() else "WAITING FOR MEDIA SESSION",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))


                    Text(
                        text = if (isMusicActive) mediaState.displayAlbum else "no data",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Top-Right Sound Quality Vertical Stack
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val isLossless = mediaState.soundQuality?.uppercase()?.contains("LOSSLESS") == true
                    val isAtmos = mediaState.soundQuality?.uppercase()?.contains("ATMOS") == true || mediaState.soundQuality?.uppercase()?.contains("DOLBY") == true

                    // Tier 1: LOSSLESS or no data
                    GeometricQualityBadge(
                        text = if (isLossless) "LOSSLESS" else if (mediaState.soundQuality != null) mediaState.soundQuality.uppercase() else "no data",
                        active = mediaState.soundQuality != null,
                        accentColor = animatedPrimaryAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tier 2: DOLBY ATMOS or no data
                    GeometricQualityBadge(
                        text = if (isAtmos) "DOLBY ATMOS" else "no data",
                        active = isAtmos,
                        accentColor = animatedSecondaryAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tier 3: no data (or source tag)
                    GeometricQualityBadge(
                        text = if (mediaState.packageName != null) "SRC: ${mediaState.displaySourceApp.uppercase()}" else "no data",
                        active = mediaState.packageName != null,
                        accentColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isMusicActive) {
                    val artworkTileShape = RoundedCornerShape(24.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.95f)
                            .aspectRatio(1f)
                            .shadow(
                                elevation = if (mediaState.isPlaying) 24.dp else 8.dp,
                                shape = artworkTileShape,
                                spotColor = animatedPrimaryAccent.copy(alpha = 0.5f)
                            )
                            .clip(artworkTileShape)
                            .background(Color(0xFF12121A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mediaState.artworkBitmap != null) {
                            Image(
                                bitmap = mediaState.artworkBitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "no data",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Idle No Music Geometric Scanning Animation
                    NoMusicGeometricAnimation(
                        modifier = Modifier
                            .fillMaxHeight(0.95f)
                            .aspectRatio(1.4f),
                        accentColor = animatedPrimaryAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTTOM SECTION: Progress Bar + Timestamps + Glowing Geometric Controls Strip
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Center-Bottom Progress Bar with Time Markers & Far Right "no data" Indicator Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (mediaState.durationMs > 0 && isMusicActive) {
                            val currentPos = if (isDragging) dragPosition.toLong() else mediaState.positionMs

                            Slider(
                                value = (if (isDragging) dragPosition else mediaState.positionMs.toFloat())
                                    .coerceIn(0f, mediaState.durationMs.toFloat()),
                                onValueChange = { newValue ->
                                    isDragging = true
                                    dragPosition = newValue
                                },
                                onValueChangeFinished = {
                                    onSeekTo(dragPosition.toLong())
                                    isDragging = false
                                },
                                valueRange = 0f..mediaState.durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedPrimaryAccent,
                                    activeTrackColor = animatedPrimaryAccent,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTimeMs(currentPos),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatTimeMs(mediaState.durationMs),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "00:00", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(text = "00:00", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Far-Right Progress Indicator Block: "no data"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "no data",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Strip: Single Horizontal Strip of Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Loop State Icon + "no data" Block
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (mediaState.repeatMode == 1) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Loop State",
                            tint = if (mediaState.repeatMode > 0) animatedPrimaryAccent else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (mediaState.repeatMode == 1) "ONE" else if (mediaState.repeatMode == 2) "ALL" else "no data",
                            color = if (mediaState.repeatMode > 0) animatedPrimaryAccent else Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Center Controls: Skip Prev, Play/Pause, Skip Next
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip Previous
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RectangleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RectangleShape)
                                .clickable { onSkipPrevious() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastRewind,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Primary Play / Pause Glowing Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(14.dp, RectangleShape, spotColor = animatedPrimaryAccent)
                                .clip(RectangleShape)
                                .background(animatedPrimaryAccent)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RectangleShape)
                                .clickable(
                                    onClick = onPlayPause,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(targetState = mediaState.isPlaying, label = "play_pause") { playing ->
                                Icon(
                                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (playing) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Skip Next
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RectangleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RectangleShape)
                                .clickable { onSkipNext() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Right: Settings Gateway Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeometricQualityBadge(
    text: String,
    active: Boolean,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) accentColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (active) accentColor else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DotGridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = 32.dp.toPx()
        val radius = 1.dp.toPx()
        val color = Color.White.copy(alpha = 0.04f)

        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                drawCircle(color = color, radius = radius, center = Offset(x, y))
                y += step
            }
            x += step
        }
    }
}

private fun formatTimeMs(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
