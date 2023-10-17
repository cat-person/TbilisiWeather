package cafe.serenity.tbilisiweather

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeatherDBO::class, WeatherPredictionDBO::class],
    version = 1
)
abstract class WeatherDB : RoomDatabase() {
    abstract val dao: WeatherDao
}