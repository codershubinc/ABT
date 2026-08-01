package com.codershubinc.abt.data

import android.graphics.Bitmap

data class MediaAppInfo(
    val packageName: String,
    val appLabel: String,
    val iconBitmap: Bitmap? = null,
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null
)

/**
 * Data representation of the active system media session.
 * 
 * STRICT DATA RULE:
 * If any metadata field is null or missing, the UI must strictly render the exact string "no data".
 */
data class MediaState(
    val isPermissionGranted: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artworkBitmap: Bitmap? = null,
    val soundQuality: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: Int = 0, // PlaybackStateCompat.REPEAT_MODE_NONE
    val shuffleMode: Int = 0, // PlaybackStateCompat.SHUFFLE_MODE_NONE
    val packageName: String? = null,
    val appLabel: String? = null,
    val appIconBitmap: Bitmap? = null,
    val hasActiveSession: Boolean = false,
    val activeApps: List<MediaAppInfo> = emptyList(),
    val isAutoMode: Boolean = true,
    val selectedPackageName: String? = null
) {
    /**
     * Helper formatting getters enforcing the STRICT DATA RULE:
     * Returns the exact value if present and non-blank, otherwise exact string "no data".
     */
    val displayTitle: String
        get() = title?.takeIf { it.isNotBlank() } ?: "no data"

    val displayArtist: String
        get() = artist?.takeIf { it.isNotBlank() } ?: "no data"

    val displayAlbum: String
        get() = album?.takeIf { it.isNotBlank() } ?: "no data"

    val displaySoundQuality: String
        get() = soundQuality?.takeIf { it.isNotBlank() } ?: "no data"

    val displaySourceApp: String
        get() {
            if (packageName == "org.kde.kdeconnect_tp") return "KDE CONNECT"
            return appLabel?.takeIf { it.isNotBlank() }
                ?: packageName?.takeIf { it.isNotBlank() }
                ?: "no data"
        }
}
