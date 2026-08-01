package com.codershubinc.abt.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("abt_preferences", Context.MODE_PRIVATE)

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean(KEY_KEEP_SCREEN_ON, true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _autoLaunchWidget = MutableStateFlow(prefs.getBoolean(KEY_AUTO_LAUNCH, true))
    val autoLaunchWidget: StateFlow<Boolean> = _autoLaunchWidget.asStateFlow()

    private val _autoSwitchAudioApps = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SWITCH_AUDIO_APPS, true))
    val autoSwitchAudioApps: StateFlow<Boolean> = _autoSwitchAudioApps.asStateFlow()

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _keepScreenOn.value = enabled
    }

    fun setAutoLaunchWidget(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LAUNCH, enabled).apply()
        _autoLaunchWidget.value = enabled
    }

    fun setAutoSwitchAudioApps(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SWITCH_AUDIO_APPS, enabled).apply()
        _autoSwitchAudioApps.value = enabled
    }

    fun isNotificationListenerGranted(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(packageName)
    }

    companion object {
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_AUTO_LAUNCH = "auto_launch"
        private const val KEY_AUTO_SWITCH_AUDIO_APPS = "auto_switch_audio_apps"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
