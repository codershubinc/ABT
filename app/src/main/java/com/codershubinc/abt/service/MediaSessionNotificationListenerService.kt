package com.codershubinc.abt.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.util.Log
import com.codershubinc.abt.data.MediaRepository

class MediaSessionNotificationListenerService : NotificationListenerService() {

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var componentName: ComponentName
    private val repository = MediaRepository.getInstance()

    private val activeSessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            onActiveControllersChanged(controllers)
        }

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        componentName = ComponentName(this, MediaSessionNotificationListenerService::class.java)
        Log.d(TAG, "MediaSessionNotificationListenerService created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected")
        repository.updatePermissionStatus(true)
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsChangedListener,
                componentName
            )
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            onActiveControllersChanged(controllers)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException getting active sessions", e)
            repository.updatePermissionStatus(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onListenerConnected", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener disconnected")
        repository.updatePermissionStatus(false)
        try {
            mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsChangedListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing listener", e)
        }
        repository.setActiveControllers(applicationContext, null)
    }

    private fun onActiveControllersChanged(controllers: List<MediaController>?) {
        Log.d(TAG, "Active media controllers changed, count: ${controllers?.size ?: 0}")
        repository.setActiveControllers(applicationContext, controllers)
    }

    companion object {
        private const val TAG = "MediaSessionService"
    }
}
