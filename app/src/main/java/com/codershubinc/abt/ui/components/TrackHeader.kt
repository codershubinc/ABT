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

            GeometricQualityBadge(
                text = if (isLossless) "LOSSLESS" else if (mediaState.soundQuality != null) mediaState.soundQuality.uppercase() else "no data",
                active = mediaState.soundQuality != null,
                accentColor = animatedPrimaryAccent
            )

            Spacer(modifier = Modifier.height(6.dp))

            GeometricQualityBadge(
                text = if (isAtmos) "DOLBY ATMOS" else "no data",
                active = isAtmos,
                accentColor = animatedSecondaryAccent
            )

            Spacer(modifier = Modifier.height(6.dp))

            GeometricQualityBadge(
                text = if (mediaState.packageName != null) "SRC: ${mediaState.displaySourceApp.uppercase()}" else "no data",
                active = mediaState.packageName != null,
                accentColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
