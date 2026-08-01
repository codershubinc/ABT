package com.codershubinc.abt.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.codershubinc.abt.utils.AppIconUtils
import com.codershubinc.abt.utils.AudioQualityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaRepository private constructor() {

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private var activeControllers: List<MediaController> = emptyList()
    private var activeController: MediaController? = null
    private var lastPlaybackState: PlaybackState? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    private var positionTickerJob: Job? = null

    private var isAutoMode: Boolean = true
    private var selectedPackageName: String? = null
    private val iconCache = mutableMapOf<String, Bitmap>()
    private val controllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()

    fun updatePermissionStatus(isGranted: Boolean) {
        _mediaState.value = _mediaState.value.copy(isPermissionGranted = isGranted)
    }

    fun setActiveController(context: Context, controller: MediaController?) {
        setActiveControllers(context, if (controller != null) listOf(controller) else emptyList())
    }

    fun setActiveControllers(context: Context, controllers: List<MediaController>?) {
        val rawControllers = controllers ?: emptyList()

        // Deduplicate controllers by packageName, preferring playing ones
        val uniqueControllers = rawControllers.groupBy { it.packageName }.map { (_, list) ->
            list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list.first()
        }

        // Unregister callbacks for removed controllers
        val currentKeys = controllerCallbacks.keys.toList()
        for (oldCtrl in currentKeys) {
            if (oldCtrl !in uniqueControllers) {
                try {
                    val cb = controllerCallbacks.remove(oldCtrl)
                    if (cb != null) {
                        oldCtrl.unregisterCallback(cb)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to unregister callback for ${oldCtrl.packageName}", e)
                }
            }
        }

        activeControllers = uniqueControllers

        // Register callbacks for active controllers
        for (controller in activeControllers) {
            if (!controllerCallbacks.containsKey(controller)) {
                val callback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        updateActiveState(context)
                    }

                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        if (state?.state == PlaybackState.STATE_PLAYING && isAutoMode) {
                            activeController = controller
                        }
                        updateActiveState(context)
                    }

                    override fun onSessionDestroyed() {
                        updateActiveState(context)
                    }

                    override fun onExtrasChanged(extras: Bundle?) {
                        updateActiveState(context)
                    }
                }
                try {
                    controller.registerCallback(callback)
                    controllerCallbacks[controller] = callback
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register callback for ${controller.packageName}", e)
                }
            }
        }

        updateActiveState(context)
    }

    fun selectApp(context: Context, packageName: String) {
        isAutoMode = false
        selectedPackageName = packageName
        val target = activeControllers.firstOrNull { it.packageName == packageName }
        if (target != null) {
            activeController = target
        }
        updateActiveState(context)
    }

    fun setAutoMode(context: Context, enabled: Boolean) {
        isAutoMode = enabled
        if (enabled) {
            selectedPackageName = null
            val playingController = activeControllers.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            }
            if (playingController != null) {
                activeController = playingController
            } else if (activeController !in activeControllers) {
                activeController = activeControllers.firstOrNull()
            }
        }
        updateActiveState(context)
    }

    private fun updateActiveState(context: Context? = null) {
        if (activeControllers.isEmpty()) {
            clearActiveController()
            return
        }

        // Determine activeController based on mode
        if (isAutoMode) {
            val playingController = activeControllers.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            }
            if (playingController != null) {
                activeController = playingController
            } else if (activeController !in activeControllers) {
                activeController = activeControllers.firstOrNull()
            }
        } else {
            val manualController = activeControllers.firstOrNull { it.packageName == selectedPackageName }
            if (manualController != null) {
                activeController = manualController
            } else {
                // Selected app is no longer active, revert to auto mode
                isAutoMode = true
                selectedPackageName = null
                activeController = activeControllers.firstOrNull {
                    it.playbackState?.state == PlaybackState.STATE_PLAYING
                } ?: activeControllers.firstOrNull()
            }
        }

        // Build activeApps info list
        val appInfoList = activeControllers.map { controller ->
            val pkg = controller.packageName
            val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING ||
                    controller.playbackState?.state == PlaybackState.STATE_FAST_FORWARDING ||
                    controller.playbackState?.state == PlaybackState.STATE_REWINDING
            val meta = controller.metadata
            val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            val remoteDeviceName = AudioQualityUtils.extractRemoteDeviceName(meta, controller.extras ?: meta?.let { AudioQualityUtils.extractExtrasFromMetadata(it) })

            var appIcon = iconCache[pkg]
            var label: String? = null
            if (context != null) {
                if (appIcon == null) {
                    appIcon = AppIconUtils.getAppIconBitmap(context, pkg)
                    if (appIcon != null) {
                        iconCache[pkg] = appIcon
                    }
                }
                try {
                    val pm = context.packageManager
                    val info = pm.getApplicationInfo(pkg, 0)
                    label = pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    // keep label null
                }
            }

            MediaAppInfo(
                packageName = pkg,
                appLabel = label ?: pkg,
                iconBitmap = appIcon ?: iconCache[pkg],
                isPlaying = isPlaying,
                title = title,
                artist = artist,
                remoteDeviceName = remoteDeviceName
            )
        }

        val currentCtrl = activeController
        if (currentCtrl != null) {
            updateFromController(currentCtrl, context = context, appInfoList = appInfoList)
        } else {
            clearActiveController()
        }
    }

    private fun clearActiveController() {
        stopPositionTicker()
        activeController = null
        lastPlaybackState = null
        _mediaState.value = _mediaState.value.copy(
            isPlaying = false,
            title = null,
            artist = null,
            album = null,
            artworkBitmap = null,
            soundQuality = null,
            positionMs = 0L,
            durationMs = 0L,
            repeatMode = 0,
            shuffleMode = 0,
            packageName = null,
            appLabel = null,
            appIconBitmap = null,
            remoteDeviceName = null,
            hasActiveSession = false,
            activeApps = emptyList(),
            isAutoMode = isAutoMode,
            selectedPackageName = selectedPackageName
        )
    }

    fun updateFromController(
        controller: MediaController?,
        metadata: MediaMetadata? = controller?.metadata,
        playbackState: PlaybackState? = controller?.playbackState,
        extras: Bundle? = controller?.extras,
        context: Context? = null,
        appInfoList: List<MediaAppInfo> = _mediaState.value.activeApps
    ) {
        if (controller == null) {
            clearActiveController()
            return
        }

        val meta = metadata ?: controller.metadata
        val pbState = playbackState ?: controller.playbackState
        lastPlaybackState = pbState
        val ex = extras ?: controller.extras ?: meta?.let { AudioQualityUtils.extractExtrasFromMetadata(it) }

        val isPlaying = pbState?.state == PlaybackState.STATE_PLAYING ||
                pbState?.state == PlaybackState.STATE_FAST_FORWARDING ||
                pbState?.state == PlaybackState.STATE_REWINDING

        val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_WRITER)
            ?: meta?.getString(MediaMetadata.METADATA_KEY_COMPOSER)
        val album = meta?.getString(MediaMetadata.METADATA_KEY_ALBUM)

        val artworkBitmap: Bitmap? = meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        val soundQuality = AudioQualityUtils.extractAudioQuality(meta, ex)
        val remoteDeviceName = AudioQualityUtils.extractRemoteDeviceName(meta, ex)

        val duration = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val currentElapsed = SystemClock.elapsedRealtime()
        val rawPos = pbState?.position ?: 0L
        val lastUpdate = pbState?.lastPositionUpdateTime ?: 0L
        val speed = pbState?.playbackSpeed ?: 1.0f
        val calculatedPosition = if (isPlaying && lastUpdate > 0L && speed > 0f) {
            (rawPos + (currentElapsed - lastUpdate) * speed).toLong()
        } else {
            rawPos
        }

        val boundedPosition = if (duration > 0L) calculatedPosition.coerceIn(0L, duration) else calculatedPosition.coerceAtLeast(0L)

        val pkg = controller.packageName
        var appLabel: String? = null
        var appIcon: Bitmap? = iconCache[pkg]
        if (context != null && pkg != null) {
            if (appIcon == null) {
                appIcon = AppIconUtils.getAppIconBitmap(context, pkg)
                if (appIcon != null) {
                    iconCache[pkg] = appIcon
                }
            }
            try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(pkg, 0)
                appLabel = pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                // Keep appLabel as null
            }
        }

        _mediaState.value = _mediaState.value.copy(
            isPlaying = isPlaying,
            title = title,
            artist = artist,
            album = album,
            artworkBitmap = artworkBitmap,
            soundQuality = soundQuality,
            positionMs = boundedPosition,
            durationMs = duration,
            packageName = pkg,
            appLabel = appLabel ?: _mediaState.value.appLabel,
            appIconBitmap = appIcon ?: _mediaState.value.appIconBitmap,
            remoteDeviceName = remoteDeviceName ?: _mediaState.value.remoteDeviceName,
            hasActiveSession = true,
            activeApps = appInfoList,
            isAutoMode = isAutoMode,
            selectedPackageName = selectedPackageName
        )

        if (isPlaying) {
            startPositionTicker()
        } else {
            stopPositionTicker()
        }
    }

    private fun startPositionTicker() {
        if (positionTickerJob?.isActive == true) return
        positionTickerJob = repositoryScope.launch {
            while (true) {
                delay(500L)
                val currentState = _mediaState.value
                if (!currentState.isPlaying) break
                val pbState = lastPlaybackState
                if (pbState != null && pbState.state == PlaybackState.STATE_PLAYING) {
                    val lastUpdate = pbState.lastPositionUpdateTime
                    val currentElapsed = SystemClock.elapsedRealtime()
                    val speed = if (pbState.playbackSpeed > 0f) pbState.playbackSpeed else 1.0f
                    val estimatedPos = if (lastUpdate > 0L) {
                        (pbState.position + (currentElapsed - lastUpdate) * speed).toLong()
                    } else {
                        pbState.position
                    }
                    val clampedPos = if (currentState.durationMs > 0L) {
                        estimatedPos.coerceIn(0L, currentState.durationMs)
                    } else {
                        estimatedPos.coerceAtLeast(0L)
                    }
                    if (clampedPos != currentState.positionMs) {
                        _mediaState.value = currentState.copy(positionMs = clampedPos)
                    }
                }
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    // Transport Actions
    fun play() {
        activeController?.transportControls?.play()
    }

    fun pause() {
        activeController?.transportControls?.pause()
    }

    fun togglePlayPause() {
        if (_mediaState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs)
    }

    companion object {
        private const val TAG = "MediaRepository"

        @Volatile
        private var INSTANCE: MediaRepository? = null

        fun getInstance(): MediaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MediaRepository().also { INSTANCE = it }
            }
        }
    }
}
