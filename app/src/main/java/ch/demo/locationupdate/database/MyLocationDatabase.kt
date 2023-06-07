package ch.demo.locationupdate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Database for storing all location data.
 */

@Database(entities = [MyLocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(MyLocationTypeConverter::class)
abstract class MyLocationDatabase : RoomDatabase() {

    abstract fun locationDao(): MyLocationDao

    companion object {
        const val DATABASE_NAME = "my-location-database"

        @Volatile
        private var INSTANCE: MyLocationDatabase? = null

        fun getInstance(context: Context): MyLocationDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it}
            }
        }

        private fun buildDatabase(context: Context): MyLocationDatabase {
            return Room.databaseBuilder(
                context,
                MyLocationDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }

}