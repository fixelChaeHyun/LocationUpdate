package ch.demo.locationupdate.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.demo.locationupdate.BuildConfig
import ch.demo.locationupdate.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Service which manages turning location updates on and off. UI clients should bind to this service
 * to access this functionality.
 *
 * This service can be started the usual way (i.e. startService), but it will also start itself when
 * the first client binds to it. Thereafter it will manage its own lifetime as follows:
 *   - While there are any bound clients, the service remains started in the background. If it was
 *     in the foreground, it will exit the foreground, cancelling any ongoing notification.
 *   - When there are no bound clients and location updates are on, the service moves to the
 *     foreground and shows an ongoing notification with the latest location.
 *   - When there are no bound clients and location updates are off, the service stops itself.
 */

class ForegroundLocationService : LifecycleService() {
    val TAG = this.javaClass.name

    private val localBinder = LocalBinder()
    private var bindCount = 0

    private var started = false
    private var isForeground = false

    private fun isBound() = bindCount > 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        handleBind()
    }

    private fun handleBind() {
        bindCount++
        // Start ourself. This will let us manage our lifetime separately from bound clients.
        startService(Intent(this, this::class.java))
    }

    override fun onUnbind(intent: Intent?): Boolean {
        bindCount--
        lifecycleScope.launch {
            // UI client can unbind because it went through a configuration change, in which case it
            // will be recreated and bind again shortly. Wait a few seconds, and if still not bound,
            // manage our lifetime accordingly.
            delay(UNBIND_DELAY_MILLIS)
            manageLifetime()
        }
        // Allow clients to rebind, in which case onRebind will be called.
        return true
    }

    private fun manageLifetime() {
        when {
            // We should not be in the foreground while UI clients are bound.
            isBound() -> exitForeground()

            // Location updates were started.
//            locationRepository.isReceivingLocationUpdates.value -> enterForeground()

            // Nothing to do, so we can stop.
            else -> stopSelf()
        }
    }

    private fun exitForeground() {
        if (isForeground) {
            isForeground = false
            stopForeground(true)
        }
    }

    private fun enterForeground() {
        if (!isForeground) {
            isForeground = true

            // Show notification with the latest location.
//            showNotification(locationRepository.lastLocation.value)
        }
    }

    private fun showNotification(location: Location?) {
        if (!isForeground) {
            return
        }
        Log.w("showNotification", "location: $location")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(location))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(location: Location?) : Notification {
        // Tapping the notification opens the app.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(this.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Include an action to stop location updates without going through the app UI.
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, this::class.java).setAction(ACTION_STOP_UPDATES),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentText = if (location != null) {
            getString(R.string.location_lat_lng, location.latitude, location.longitude)
        } else {
            getString(R.string.waiting_for_location)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_location)
            .addAction(R.drawable.ic_stop, getString(R.string.stop), stopIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
    }

    fun startLocationUpdates() {
//        locationRepository.startLocationUpdates()
    }

    fun stopLocationUpdates() {
//        locationRepository.stopLocationUpdates()
    }

    /** Binder which provides clients access to the service. */
    internal inner class LocalBinder : Binder() {
        fun getService(): ForegroundLocationService = this@ForegroundLocationService
    }

    private companion object {
        const val UNBIND_DELAY_MILLIS = 2000.toLong() // 2 seconds
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "LocationUpdates"
        const val ACTION_STOP_UPDATES = BuildConfig.APPLICATION_ID + ".ACTION_STOP_UPDATES"
    }
}