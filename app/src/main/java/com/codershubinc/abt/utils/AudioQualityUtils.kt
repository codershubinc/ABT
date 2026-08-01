package com.codershubinc.abt.utils

import android.media.MediaMetadata
import android.os.Bundle
import android.util.Log

object AudioQualityUtils {

    private const val TAG = "AudioQualityUtils"

    fun extractAudioQuality(metadata: MediaMetadata?, extras: Bundle?): String? {
        val candidateBundles = mutableListOf<Bundle>()
        extras?.let { candidateBundles.add(it) }
        metadata?.let { extractExtrasFromMetadata(it)?.let { b -> candidateBundles.add(b) } }

        for (bundle in candidateBundles) {
            for (key in bundle.keySet()) {
                Log.d(TAG, "Found Key: $key | Value: ${bundle.get(key)}")
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

    fun extractRemoteDeviceName(metadata: MediaMetadata?, extras: Bundle?): String? {
        val candidateBundles = mutableListOf<Bundle>()
        extras?.let { candidateBundles.add(it) }
        metadata?.let { extractExtrasFromMetadata(it)?.let { b -> candidateBundles.add(b) } }

        val targetKeys = listOf(
            "device_name", "devicename", "remote_device", "remotedevice",
            "device", "kdeconnect.device", "kdeconnect.device_name",
            "com.kde.connect.device_name", "org.kde.kdeconnect.device_name",
            "android.subtext"
        )

        for (bundle in candidateBundles) {
            for (key in bundle.keySet()) {
                val lowerKey = key.lowercase()
                if (targetKeys.any { lowerKey.contains(it) }) {
                    val value = bundle.get(key)
                    if (value != null) {
                        val strVal = value.toString().trim()
                        if (strVal.isNotBlank() && strVal.lowercase() != "kde connect") {
                            return strVal
                        }
                    }
                }
            }
        }
        return null
    }

    fun extractExtrasFromMetadata(metadata: MediaMetadata): Bundle? {
        return try {
            val field = MediaMetadata::class.java.getDeclaredField("mBundle")
            field.isAccessible = true
            field.get(metadata) as? Bundle
        } catch (e: Exception) {
            null
        }
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

        val cleanKey = key
            .replace("android.media.metadata.", "")
            .replace("com.apple.android.music.playback.playbackstate.", "")
            .replace("com.apple.android.music.", "")
            .replace("com.apple.music.", "")
            .replace("com.spotify.", "")
            .replace("PLAYBACK_STATE_EXTRA_", "")
            .replace("_", " ")
            .uppercase()

        return "$cleanKey: ${formatSampleRate(upperVal)}"
    }

    private fun formatSampleRate(rawValue: String?): String {
        if (rawValue.isNullOrBlank()) {
            return "no data"
        }

        return try {
            val rate = rawValue.toFloat()
            if (rate >= 1000) {
                val kHz = rate / 1000
                if (kHz % 1.0f == 0.0f) {
                    "${kHz.toInt()} kHz"
                } else {
                    "%.1f kHz".format(kHz)
                }
            } else {
                "${rate.toInt()} Hz"
            }
        } catch (e: NumberFormatException) {
            "no data"
        }
    }
}
