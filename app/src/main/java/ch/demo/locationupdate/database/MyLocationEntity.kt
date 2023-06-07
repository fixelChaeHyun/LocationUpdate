package ch.demo.locationupdate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.util.*

@Entity(tableName = "my_location_table")
class MyLocationEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val foreground: Boolean = true,
    val date: Date = Date()
) {
    override fun toString(): String {
        val appState = if (foreground) {
            "[Fore]"
        } else {
            "[Back]"
        }
        return "($latitude, $longitude) $appState on ${DateFormat.getDateTimeInstance().format(date)}.\n"
    }
}