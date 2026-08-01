package com.codershubinc.abt.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.codershubinc.abt.data.MediaState
import com.codershubinc.abt.ui.components.DotGridBackground
import com.codershubinc.abt.ui.components.MusicControls
import com.codershubinc.abt.ui.components.NoMusicGeometricAnimation
import com.codershubinc.abt.ui.components.TrackHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MusicWidgetScreen(
    mediaState: MediaState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectApp: (String) -> Unit = {},
    onSelectAutoMode: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTabletOrWidescreen = configuration.screenWidthDp >= 600 || configuration.screenWidthDp > configuration.screenHeightDp

    // Dynamic Color Extraction via Palette API
    var primaryAccent by remember { mutableStateOf(Color(0xFF3D5AFE)) }
    var secondaryAccent by remember { mutableStateOf(Color(0xFF00F0FF)) }

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

        // Full Screen Structural Geometric Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = if (isTabletOrWidescreen) 32.dp else 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Track Header & Quality Stack
            TrackHeader(
                mediaState = mediaState,
                isTabletOrWidescreen = isTabletOrWidescreen,
                animatedPrimaryAccent = animatedPrimaryAccent,
                animatedSecondaryAccent = animatedSecondaryAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MIDDLE SECTION: Artwork Card / Geometric Scanner
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

            // BOTTOM SECTION: Progress Bar + Timestamps + Controls + Single App Selector Button
            MusicControls(
                mediaState = mediaState,
                animatedPrimaryAccent = animatedPrimaryAccent,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeekTo = onSeekTo,
                onSelectApp = onSelectApp,
                onSelectAutoMode = onSelectAutoMode,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}
