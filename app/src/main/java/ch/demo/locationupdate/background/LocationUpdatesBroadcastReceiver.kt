package ch.demo.locationupdate.background

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ch.demo.locationupdate.LocationRepository
import ch.demo.locationupdate.database.MyLocationEntity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import java.util.*
import java.util.concurrent.Executors

private const val TAG = "LUBroadcastReceiver"

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O and above
 * PendingIntent#getBroadcast(Context, int, Intent, int) should be used when
 * requesting location updates in the background.
 * Due to limits on background services, PendingIntent#getService(Context, int, Intent, int)
 * should NOT be used.
 *
 * NOte: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the LocationRequest when the app is no longer
 * in the foreground.
 */

/**
 * Broadcast 의 Intent 로 전달 되는 좌표 값을 Database 에 저장 시키는 역할.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive() context: $context, intent: $intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {
            // com.google.android.gms.location.EXTRA_LOCATION_RESULT -> Google Library 에서 사용 중인 key 값
            Log.e(TAG, "extra: ${intent.extras?.getParcelable<LocationResult>("com.google.android.gms.location.EXTRA_LOCATION_RESULT")}")

            // Checks for location availability changes.
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(TAG, "** Location services are no longer available!")
                } else {
                    Log.i(TAG, "** Location services are AVAILABLE NOW !!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.mapIndexed { index, location ->
//                    if (index == 0) {
//                        Log.d(TAG, "-> locations[0] : ${location.latitude}, ${location.longitude}\n-> $location")
//                    }
                    Log.d(TAG, "-> locations[$index] : ${location.latitude}, ${location.longitude}\n-> $location")
                }
                val last = locationResult.lastLocation
                Log.w(TAG, "lastLocation: (${last.latitude}, ${last.longitude})")
                val entity = MyLocationEntity(
                    latitude = last.latitude,
                    longitude = last.longitude,
                    foreground = isAppInForeground(context),
                    date = Date(last.time)
                )

                LocationRepository.getInstance(context, Executors.newSingleThreadExecutor())
                    .addLocation(entity)
            }
        }
    }

    // Note: This function's implementation is only for debugging purposes. If you are going to do
    // this in a production app, you should instead track the state of all your activities in a
    // process via android.app.Application.ActivityLifecycleCallbacks's
    // unregisterActivityLifecycleCallbacks(). For more information, check out the link:
    // https://developer.android.com/reference/android/app/Application.html#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        appProcesses.forEach { appProcess ->
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }
    companion object {
        const val ACTION_PROCESS_UPDATES =
            "ch.demo.locationupdate.ACTION_PROCESS_UPDATES"
    }
}