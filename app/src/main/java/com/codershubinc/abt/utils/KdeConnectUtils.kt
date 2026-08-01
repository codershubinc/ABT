package com.codershubinc.abt.utils

import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Bundle

object KdeConnectUtils {

    const val PACKAGE_NAME = "org.kde.kdeconnect_tp"

    fun isKdeConnect(packageName: String?): Boolean {
        return packageName == PACKAGE_NAME
    }

    fun formatSourceLabel(packageName: String?, appLabel: String?, remoteDeviceName: String?): String {
        if (isKdeConnect(packageName)) {
            return if (!remoteDeviceName.isNullOrBlank()) {
                "KDE CONNECT ($remoteDeviceName)"
            } else {
                "KDE CONNECT"
            }
        }
        return appLabel?.takeIf { it.isNotBlank() }
            ?: packageName?.takeIf { it.isNotBlank() }
            ?: "no data"
    }

    fun isKdeConnectPlaybackActive(packageName: String?, pbState: PlaybackState?, title: String?): Boolean {
        if (!isKdeConnect(packageName)) return false
        return !title.isNullOrBlank() && pbState?.state != PlaybackState.STATE_STOPPED
    }

    fun extractRemoteDeviceName(metadata: MediaMetadata?, extras: Bundle?): String? {
        val candidateBundles = mutableListOf<Bundle>()
        extras?.let { candidateBundles.add(it) }
        metadata?.let { AudioQualityUtils.extractExtrasFromMetadata(it)?.let { b -> candidateBundles.add(b) } }

        val targetKeys = listOf(
            "device_name", "devicename", "remote_device", "remotedevice",
            "device", "kdeconnect.device", "kdeconnect.device_name",
            "com.kde.connect.device_name", "org.kde.kdeconnect.device_name",
            "android.subtext", "subtext", "notification.subtext"
        )

        for (bundle in candidateBundles) {
            for (key in bundle.keySet()) {
                val lowerKey = key.lowercase()
                if (targetKeys.any { lowerKey.contains(it) }) {
                    val value = bundle.get(key)
                    if (value != null) {
                        val strVal = value.toString().trim()
                        if (strVal.isNotBlank() && strVal.lowercase() != "kde connect" && !strVal.lowercase().contains("media control")) {
                            return strVal
                        }
                    }
                }
            }
        }

        // Secondary check: Metadata fields
        metadata?.getString(MediaMetadata.METADATA_KEY_COMPOSER)?.let {
            if (it.isNotBlank() && !it.lowercase().contains("kde")) return it.trim()
        }
        metadata?.getString(MediaMetadata.METADATA_KEY_WRITER)?.let {
            if (it.isNotBlank() && !it.lowercase().contains("kde")) return it.trim()
        }

        return null
    }
}
