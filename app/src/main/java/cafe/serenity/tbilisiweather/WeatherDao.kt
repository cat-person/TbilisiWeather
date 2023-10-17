package cafe.serenity.tbilisiweather

import androidx.room.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Dao
interface WeatherDao {
    @Upsert
    suspend fun upsertWeatherData(weather: WeatherDBO)

    @Upsert
    suspend fun upsertWeatherPredictionDataBatch(weather: List<WeatherPredictionDBO>)

    @Query("SELECT * FROM Weather WHERE dateSecondsFromEpoch BETWEEN :fromSecondsFromEpoch AND :toSecondsFromEpoch")
    suspend fun getPredictionsByPeriod(
        fromSecondsFromEpoch: Long,
        toSecondsFromEpoch: Long
    ): List<WeatherDBO>

    @Query("SELECT * FROM Weather WHERE dateSecondsFromEpoch BETWEEN :fromSecondsFromEpoch AND :toSecondsFromEpoch")
    suspend fun getHistoricalByPeriod(
        fromSecondsFromEpoch: Long,
        toSecondsFromEpoch: Long
    ): List<WeatherDBO>

    suspend fun getLastSaved(): WeatherDBO? {
        return getHistoricalByPeriod(
            0,
            LocalDateTime.now().plusMinutes(5).toEpochSecond(ZoneOffset.UTC)
        ).maxByOrNull { it.dateSecondsFromEpoch }
    }
}

@Entity(tableName = "weather")
data class WeatherDBO(
    val dateSecondsFromEpoch: Long,
    val temperatureK: Float,
    val weatherCode: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

@Entity(tableName = "weatherPrediction")
data class WeatherPredictionDBO(
    val dateOfForecastSecondsFromEpoch: Long,
    val dateSecondsFromEpoch: Long,
    val temperatureK: Float,
    val weatherCode: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)