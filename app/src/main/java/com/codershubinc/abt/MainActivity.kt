package com.codershubinc.abt

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.codershubinc.abt.data.MediaRepository
import com.codershubinc.abt.data.SettingsRepository
import com.codershubinc.abt.ui.screens.MusicWidgetScreen
import com.codershubinc.abt.ui.screens.SettingsScreen
import com.codershubinc.abt.ui.theme.ABTTheme
import com.codershubinc.abt.ui.theme.VoidBackground

enum class ABTScreen {
    SETTINGS,
    MUSIC_WIDGET
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mediaRepository: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsRepository = SettingsRepository.getInstance(this)
        mediaRepository = MediaRepository.getInstance()

        // Hide status bar and navigation pillars for fluid edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setupImmersiveMode()

        setContent {
            ABTTheme {
                val mediaState by mediaRepository.mediaState.collectAsState()
                val keepScreenOn by settingsRepository.keepScreenOn.collectAsState()
                val autoLaunchWidget by settingsRepository.autoLaunchWidget.collectAsState()
                val autoSwitchAudioApps by settingsRepository.autoSwitchAudioApps.collectAsState()

                // Dynamic Keep Screen On flag management
                LaunchedEffect(keepScreenOn) {
                    if (keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                // Initial Screen Routing based on permission and auto-launch preference
                var currentScreen by remember {
                    val isGranted = settingsRepository.isNotificationListenerGranted()
                    val initial = if (isGranted && autoLaunchWidget) ABTScreen.MUSIC_WIDGET else ABTScreen.SETTINGS
                    mutableStateOf(initial)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoidBackground
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            ABTScreen.SETTINGS -> {
                                SettingsScreen(
                                    isPermissionGranted = mediaState.isPermissionGranted,
                                    keepScreenOn = keepScreenOn,
                                    autoLaunchWidget = autoLaunchWidget,
                                    autoSwitchAudioApps = autoSwitchAudioApps,
                                    onKeepScreenOnChanged = { enabled ->
                                        settingsRepository.setKeepScreenOn(enabled)
                                    },
                                    onAutoLaunchWidgetChanged = { enabled ->
                                        settingsRepository.setAutoLaunchWidget(enabled)
                                    },
                                    onAutoSwitchAudioAppsChanged = { enabled ->
                                        settingsRepository.setAutoSwitchAudioApps(enabled)
                                        mediaRepository.setAutoMode(this@MainActivity, enabled)
                                    },
                                    onNavigateToWidget = {
                                        currentScreen = ABTScreen.MUSIC_WIDGET
                                    },
                                    onRefreshPermission = {
                                        refreshPermissionState()
                                    }
                                )
                            }

                            ABTScreen.MUSIC_WIDGET -> {
                                MusicWidgetScreen(
                                    mediaState = mediaState,
                                    onPlayPause = { mediaRepository.togglePlayPause() },
                                    onSkipNext = { mediaRepository.skipToNext() },
                                    onSkipPrevious = { mediaRepository.skipToPrevious() },
                                    onSeekTo = { pos -> mediaRepository.seekTo(pos) },
                                    onSelectApp = { pkg -> mediaRepository.selectApp(this@MainActivity, pkg) },
                                    onSelectAutoMode = { mediaRepository.setAutoMode(this@MainActivity, true) },
                                    onNavigateToSettings = {
                                        currentScreen = ABTScreen.SETTINGS
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        val granted = settingsRepository.isNotificationListenerGranted()
        mediaRepository.updatePermissionStatus(granted)
    }

    private fun setupImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}