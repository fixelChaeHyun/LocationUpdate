package ch.demo.locationupdate

import android.content.Context
import ch.demo.locationupdate.database.MyLocationDatabase
import ch.demo.locationupdate.database.MyLocationEntity
import java.util.concurrent.ExecutorService

/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and checking location update status).
 */
class LocationRepository private constructor(
    private val myLocationDataBase: MyLocationDatabase,
    private val myLocationManager: MyLocationManager,
    private val executor: ExecutorService
){

    private val locationDao = myLocationDataBase.locationDao()

    fun addLocations(myLocationsEntities: List<MyLocationEntity>) {
        executor.execute {
            locationDao.addLocations(myLocationsEntities)
        }
    }

    fun addLocation(myLocationEntity: MyLocationEntity) {
        executor.execute {
            locationDao.addLocation(myLocationEntity)
        }
    }

    fun deleteAllLocationInDB() {

    }

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