package com.codershubinc.abt.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codershubinc.abt.ui.components.DotGridBackground
import com.codershubinc.abt.ui.components.InfoRow
import com.codershubinc.abt.ui.components.ModernCard
import com.codershubinc.abt.ui.components.NullVoidToggleRow
import com.codershubinc.abt.ui.theme.VoidDividerColor

@Composable
fun SettingsScreen(
    isPermissionGranted: Boolean,
    keepScreenOn: Boolean,
    autoLaunchWidget: Boolean,
    autoSwitchAudioApps: Boolean = true,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onAutoLaunchWidgetChanged: (Boolean) -> Unit,
    onAutoSwitchAudioAppsChanged: (Boolean) -> Unit = {},
    onNavigateToWidget: () -> Unit,
    onRefreshPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040406))
    ) {
        // NullVoid Dot Grid Pattern
        DotGridBackground(modifier = Modifier.fillMaxSize())

        // Top-Left Radial Glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3D5AFE).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = if (isTablet) 64.dp else 24.dp)
                .then(
                    if (isTablet) Modifier.widthIn(max = 800.dp).align(Alignment.TopCenter)
                    else Modifier
                )
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onNavigateToWidget() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header Title
            Text(
                text = "SETTINGS",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Configure your NullVoid desk gadget experience",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Section 1: Media Interceptor Permission
                Text(
                    text = "MEDIA INTERCEPTOR",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                ModernCard {
                    InfoRow(
                        icon = if (isPermissionGranted) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                        label = "Notification Listener",
                        value = if (isPermissionGranted) "Verified & Listening" else "Permission Required",
                        valueColor = if (isPermissionGranted) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        onClick = {
                            if (!isPermissionGranted) {
                                openNotificationListenerSettings(context)
                                onRefreshPermission()
                            }
                        },
                        showChevron = !isPermissionGranted
                    )

                    if (!isPermissionGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ABT requires BIND_NOTIFICATION_LISTENER_SERVICE permission to capture real-time media metadata & lossless audio quality details.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Gadget Behavior
                Text(
                    text = "GADGET BEHAVIOR",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                ModernCard {
                    NullVoidToggleRow(
                        icon = Icons.Rounded.DisplaySettings,
                        label = "Display Lock",
                        description = "Keep Screen On",
                        checked = keepScreenOn,
                        onCheckedChange = onKeepScreenOnChanged
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = VoidDividerColor
                    )

                    NullVoidToggleRow(
                        icon = Icons.Rounded.PlayArrow,
                        label = "Auto Navigation",
                        description = "Auto-Launch Music Widget",
                        checked = autoLaunchWidget,
                        onCheckedChange = onAutoLaunchWidgetChanged
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = VoidDividerColor
                    )

                    NullVoidToggleRow(
                        icon = Icons.Rounded.Autorenew,
                        label = "Auto Switch Mode",
                        description = "Auto-Switch Playing Audio Apps",
                        checked = autoSwitchAudioApps,
                        onCheckedChange = onAutoSwitchAudioAppsChanged
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: System Information
                Text(
                    text = "SYSTEM",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                ModernCard {
                    InfoRow(
                        icon = Icons.Rounded.Info,
                        label = "Protocol",
                        value = "NullVoid ABT v1.0",
                        onClick = null,
                        showChevron = false
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // Save / Launch Button
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3D5AFE))
                    .clickable { onNavigateToWidget() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LAUNCH MUSIC WIDGET",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

private fun openNotificationListenerSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
