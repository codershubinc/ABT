package com.codershubinc.abt.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codershubinc.abt.data.MediaState

@Composable
fun TrackHeader(
    mediaState: MediaState,
    isTabletOrWidescreen: Boolean,
    animatedPrimaryAccent: Color,
    animatedSecondaryAccent: Color,
    modifier: Modifier = Modifier
) {
    val isMusicActive = mediaState.hasActiveSession && (mediaState.isPlaying || mediaState.title != null)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Top-Left Metadata
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

            // Massive Track Title
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

            // Album
            if (isMusicActive && !mediaState.album.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mediaState.album,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Top-Right Sound Quality Vertical Stack (Only show active data tiles)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quality = mediaState.soundQuality
            if (!quality.isNullOrBlank()) {
                val isLossless = quality.uppercase().contains("LOSSLESS")
                GeometricQualityBadge(
                    text = if (isLossless) "LOSSLESS" else quality.uppercase(),
                    active = true,
                    accentColor = animatedPrimaryAccent
                )
            }

            val isAtmos = quality?.uppercase()?.contains("ATMOS") == true ||
                    quality?.uppercase()?.contains("DOLBY") == true
            if (isAtmos) {
                GeometricQualityBadge(
                    text = "DOLBY ATMOS",
                    active = true,
                    accentColor = animatedSecondaryAccent
                )
            }

            if (mediaState.packageName != null) {
                GeometricQualityBadge(
                    text = "SRC: ${mediaState.displaySourceApp.uppercase()}",
                    active = true,
                    accentColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}
