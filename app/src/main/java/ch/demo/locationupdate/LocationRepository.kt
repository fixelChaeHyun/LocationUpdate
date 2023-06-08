package ch.demo.locationupdate

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import ch.demo.locationupdate.database.MyLocationDatabase
import ch.demo.locationupdate.database.MyLocationEntity
import java.util.*
import java.util.concurrent.ExecutorService

/**
 * Access point for database (MyLocation data) and location APIs
 * (start/stop location updates and checking location update status).
 */
class LocationRepository private constructor(
    private val myLocationDataBase: MyLocationDatabase,
    private val myLocationManager: MyLocationManager,
    private val executor: ExecutorService
){

    private val locationDao = myLocationDataBase.locationDao()

    /**
     * Returns all recorded locations from database.
     */
    fun getLocations(): LiveData<List<MyLocationEntity>> = locationDao.getLocations()

    /**
     * Returns specific location in database.
     */
    fun getLocation(id: UUID): LiveData<MyLocationEntity> = locationDao.getLocation(id)

    /**
     * Updates location in database.
     */
    fun updateLocation(myLocationEntity: MyLocationEntity) {
        executor.execute {
            locationDao.updateLocation(myLocationEntity)
        }
    }

    /**
     * Adds list of location to the database.
     */
    fun addLocations(myLocationsEntities: List<MyLocationEntity>) {
        executor.execute {
            locationDao.addLocations(myLocationsEntities)
        }
    }

    /**
     * Adds location to the database.
     */
    fun addLocation(myLocationEntity: MyLocationEntity) {
        executor.execute {
            locationDao.addLocation(myLocationEntity)
        }
    }

    fun deleteAllLocationInDB() {
        executor.execute {
            locationDao.deleteLocationTable()
        }

    }

    /**
     * Status of whether the app is actively subscribed to location changes.
     */
    val receivingLocationUpdates: LiveData<Boolean> = myLocationManager.receivingLocationUpdates

    /**
     * Subscribes to location updates.
     */
    @MainThread
    fun startLocationUpdates() = myLocationManager.startLocationUpdates()

    /**
     * Un-subscribes from location updates.
     */
    fun stopLocationUpdates() = myLocationManager.stopLocationUpdates()

    companion object {
        @Volatile private var INSTANCE: LocationRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): LocationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationRepository(
                    MyLocationDatabase.getInstance(context),
                    MyLocationManager.getInstance(context),
                    executor)
                    .also { INSTANCE = it }
            }
        }
    }

}