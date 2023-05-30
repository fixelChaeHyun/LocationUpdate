package ch.demo.locationupdate.foreground

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import javax.inject.Inject

class ForegroundLocationServiceConnection @Inject constructor() : ServiceConnection {
    var service: ForegroundLocationService? = null
        private set

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as ForegroundLocationService.LocalBinder).getService()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        // Note: this should never be called since the service is in the same process.
        service = null
    }
}