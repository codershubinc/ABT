package com.codershubinc.abt.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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
            scanExistingActiveNotifications()
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

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        processNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        if (sbn.packageName == "org.kde.kdeconnect_tp") {
            Log.d(TAG, "KDE Connect notification removed")
        }
    }

    private fun scanExistingActiveNotifications() {
        try {
            val notifications = activeNotifications
            if (notifications != null) {
                for (sbn in notifications) {
                    processNotification(sbn)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning existing active notifications", e)
        }
    }

    private fun processNotification(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val extras = sbn.notification?.extras ?: return

        // 1. Check for MediaSession Token in Notification
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
        }

        if (token != null) {
            try {
                val controller = MediaController(applicationContext, token)
                repository.registerNotificationMediaController(applicationContext, controller)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating MediaController from token", e)
            }
        }

        // 2. Extract notification metadata (especially for KDE Connect or media notifications)
        if (pkg == "org.kde.kdeconnect_tp" || sbn.notification.category == Notification.CATEGORY_TRANSPORT) {
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(Notification.EXTRA_LARGE_ICON, Bitmap::class.java)
                    ?: extras.getParcelable(Notification.EXTRA_PICTURE, Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                (extras.getParcelable(Notification.EXTRA_LARGE_ICON) as? Bitmap)
                    ?: (extras.getParcelable(Notification.EXTRA_PICTURE) as? Bitmap)
            }

            if (!title.isNullOrBlank() || !text.isNullOrBlank()) {
                repository.updateFromNotificationFallback(
                    context = applicationContext,
                    packageName = pkg,
                    title = title,
                    text = text,
                    subText = subText,
                    largeIcon = icon
                )
            }
        }
    }

    private fun onActiveControllersChanged(controllers: List<MediaController>?) {
        Log.d(TAG, "Active media controllers changed, count: ${controllers?.size ?: 0}")
        repository.setActiveControllers(applicationContext, controllers)
    }

    companion object {
        private const val TAG = "MediaSessionService"
    }
}
