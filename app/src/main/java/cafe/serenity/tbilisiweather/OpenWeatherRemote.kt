package cafe.serenity.tbilisiweather

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// For a test project it's practical to hardcode those 2 parameters
val tbilisiLocation = LatLng(41.69411, 44.83368)

class OpenWeatherRemote(private val openWeatherApiKey: String) {
    private val ktorHttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    suspend fun getDailyForecast(): Result<ForecastDTO> {
        val data: HttpResponse =
            ktorHttpClient.get("https://api.openweathermap.org/data/2.5/forecast?lat=${tbilisiLocation.latitudeDeg}&lon=${tbilisiLocation.longitudeDeg}&appid=$openWeatherApiKey")
        return when (data.status) {
            HttpStatusCode.OK, HttpStatusCode.Accepted, HttpStatusCode.Created -> Result.Success(
                data.body()
            )

            else -> Result.Failure(data.status, data.body())
        }
    }

    suspend fun getCurrentWeather(): Result<WeatherDTO> {
        val data: HttpResponse =
            ktorHttpClient.get("https://api.openweathermap.org/data/2.5/weather?lat=${tbilisiLocation.latitudeDeg}&lon=${tbilisiLocation.longitudeDeg}&appid=$openWeatherApiKey")
        return when (data.status) {
            HttpStatusCode.OK, HttpStatusCode.Accepted, HttpStatusCode.Created -> Result.Success(
                data.body()
            )

            else -> Result.Failure(data.status, data.body())
        }
    }

    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val value: T) : Result<T>()
        data class Failure(val statusCode: HttpStatusCode, val message: String?) : Result<Nothing>()
    }
}


@Serializable
data class ForecastDTO(
    @SerialName("cod") val code: String, @SerialName("list") val data: List<WeatherDTO>
)

@Serializable
data class WeatherDTO(
    // Have used the name from the API need to figure out smthng better
    @SerialName("dt") val dateTicks: Long,
    @SerialName("main") val temperaturePressure: TemperaturePressureDTO,
    @SerialName("weather") val weatherType: List<WeatherTypeDTO>,
    @SerialName("wind") val wind: WindDTO,
)

data class LatLng(val latitudeDeg: Double, val longitudeDeg: Double)

@Serializable
data class TemperaturePressureDTO(
    @SerialName("temp") val temperatureK: Float, @SerialName("pressure") val pressure: Int
)

@Serializable
data class WeatherTypeDTO(
    @SerialName("id") val id: Int,
)

@Serializable
data class WindDTO(
    @SerialName("speed") val speedMS: Float, @SerialName("deg") val deg: Int
)