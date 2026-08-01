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

    private var activeController: MediaController? = null
    private var lastPlaybackState: PlaybackState? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    private var positionTickerJob: Job? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController, metadata = metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController, playbackState = state)
        }

        override fun onSessionDestroyed() {
            Log.d(TAG, "MediaSession destroyed")
            clearActiveController()
        }

        override fun onExtrasChanged(extras: Bundle?) {
            updateFromController(activeController, extras = extras)
        }
    }

    fun updatePermissionStatus(isGranted: Boolean) {
        _mediaState.value = _mediaState.value.copy(isPermissionGranted = isGranted)
    }

    fun setActiveController(context: Context, controller: MediaController?) {
        if (activeController?.sessionToken == controller?.sessionToken && controller != null) {
            updateFromController(controller)
            return
        }

        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister previous controller callback", e)
        }

        activeController = controller

        if (controller != null) {
            try {
                controller.registerCallback(controllerCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register controller callback", e)
            }
            updateFromController(controller, context = context)
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
            hasActiveSession = false
        )
    }

    fun updateFromController(
        controller: MediaController?,
        metadata: MediaMetadata? = controller?.metadata,
        playbackState: PlaybackState? = controller?.playbackState,
        extras: Bundle? = controller?.extras,
        context: Context? = null
    ) {
        if (controller == null) {
            clearActiveController()
            return
        }

        val meta = metadata ?: controller.metadata
        val pbState = playbackState ?: controller.playbackState
        lastPlaybackState = pbState
        val ex = extras ?: controller.extras ?: meta?.let { extractExtrasFromMetadata(it) }

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

        val soundQuality = extractAudioQuality(meta, ex)

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
        if (context != null && pkg != null) {
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
            hasActiveSession = true
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

    private fun extractExtrasFromMetadata(metadata: MediaMetadata): Bundle? {
        return try {
            val field = MediaMetadata::class.java.getDeclaredField("mBundle")
            field.isAccessible = true
            field.get(metadata) as? Bundle
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAudioQuality(metadata: MediaMetadata?, extras: Bundle?): String? {
        val candidateBundles = mutableListOf<Bundle>()
        extras?.let { candidateBundles.add(it) }
        metadata?.let { extractExtrasFromMetadata(it)?.let { b -> candidateBundles.add(b) } }

        for (bundle in candidateBundles) {
            for (key in bundle.keySet()) {
                Log.d("ABT_AudioDump", "Found Key: $key | Value: ${bundle.get(key)}")
                val lowerKey = key.lowercase()
                if (lowerKey.contains("quality") ||
                    lowerKey.contains("lossless") ||
                    lowerKey.contains("codec") ||
                    lowerKey.contains("format") ||
                    lowerKey.contains("atmos") ||
                    lowerKey.contains("spatial") ||
                    lowerKey.contains("bitrate") ||
                    lowerKey.contains("sample_rate") ||
                    lowerKey.contains("audio_type")
                ) {
                    val value = bundle.get(key)
                    if (value != null) {
                        val strVal = value.toString().trim()
                        if (strVal.isNotBlank() && strVal != "0" && strVal != "false") {
                            return formatQualityString(key, strVal)
                        }
                    }
                }
            }

            // Inspect all string/boolean values in bundle for known quality indicators
            for (key in bundle.keySet()) {
                val valObj = bundle.get(key) ?: continue
                val str = valObj.toString()
                if (containsQualityKeyword(str)) {
                    return str.uppercase()
                }
            }
        }

        return null
    }

    private fun containsQualityKeyword(str: String): Boolean {
        val upper = str.uppercase()
        return upper.contains("LOSSLESS") ||
                upper.contains("HI-RES") ||
                upper.contains("HIRES") ||
                upper.contains("DOLBY") ||
                upper.contains("ATMOS") ||
                upper.contains("SPATIAL") ||
                upper.contains("FLAC") ||
                upper.contains("24-BIT") ||
                upper.contains("96KHZ") ||
                upper.contains("192KHZ") ||
                upper.contains("320KBPS") ||
                upper.contains("MASTER")
    }

    private fun formatQualityString(key: String, value: String): String {
        val upperVal = value.uppercase()
        if (containsQualityKeyword(upperVal)) {
            return upperVal
        }
        val cleanKey = key.replace("android.media.metadata.", "")
            .replace("com.apple.music.", "")
            .replace("com.spotify.", "")
            .replace("_", " ")
            .uppercase()
        return "$cleanKey: $upperVal"
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
