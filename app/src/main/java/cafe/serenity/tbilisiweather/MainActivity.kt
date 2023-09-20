package cafe.serenity.tbilisiweather

import io.ktor.client.request.*
import io.ktor.client.statement.*

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.serenity.tbilisiweather.ui.theme.TbilisiWeatherTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val httpClient = HttpClient()

        MainScope().launch {
            val response = ktorHttpClient.get<ForecastDTO>("https://api.openweathermap.org/data/2.5/forecast?lat=41.69411&lon=44.83368&appid=a58c4682a49e63fb623ba14dce0dd7b4")
            setContent {
                TbilisiWeatherTheme {
                    val forecast = Forecast()
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.hsl(190f, 0.5f, 0.7f, 1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CurrentWeatherWidget(forecast.currentWeather, Modifier.fillMaxWidth())
                            TodayWeatherWidget(forecast.todayForecast)
                            WeaklyWeatherWidget(response.toForecastVM())
                        }
                    }
                }
            }
        }

        setContent {
            TbilisiWeatherTheme {
                val forecast = Forecast()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.hsl(190f, 0.5f, 0.7f, 1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CurrentWeatherWidget(forecast.currentWeather, Modifier.fillMaxWidth())
                        TodayWeatherWidget(forecast.todayForecast)
                        WeaklyWeatherWidget(listOf())
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherWidget(weather: Weather11, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxWidth().padding(0.dp, 32.dp, 0.dp, 16.dp)) {
        Text(
            text = "Tbilisi",
            fontSize = 48.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = "${weather.weatherType.toEmoji()} ${weather.temperatureC}°",
            fontSize = 64.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
fun TodayWeatherWidget(todayWeather: Map<Int, Weather11>, modifier: Modifier = Modifier) {

    Card(modifier = modifier) {
        LazyRow(modifier = Modifier.fillMaxWidth()) {

            val keys = todayWeather.keys.toIntArray()
            items(keys.size) { itemIdx ->
                val key = keys[itemIdx]
                val weather = todayWeather[key]
                Column {
                    Text(
                        text = "${weather?.weatherType?.toEmoji()} ${weather?.temperatureC}°",
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "${key}:00",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Divider(color = Color.Red)
            }
        }
    }
}


@Composable
fun WeaklyWeatherWidget(forecast: List<WeatherPredicationVM>, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(forecast.size) { itemIdx ->
                val weather = forecast[itemIdx]

                Row {
                    Text(
                        text = "${weather.date}",
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "${weather?.weatherType?.toEmoji()} ${weather.temperature.celsius}C°",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Divider(color = Color.White)
            }
        }
    }
}

class Weather11(val temperatureC: Int, val weatherType: WeatherType)

class Forecast {
    val currentWeather = Weather11(24, WeatherType.Cloudy)
    val todayForecast = mapOf<Int, Weather11>(
        0 to Weather11(24, WeatherType.Cloudy),
        2 to Weather11(24, WeatherType.Sunny),
        4 to Weather11(24, WeatherType.Cloudy),
        6 to Weather11(24, WeatherType.Rainy),
        8 to Weather11(24, WeatherType.Cloudy),
        10 to Weather11(24, WeatherType.Snowy),
        12 to Weather11(24, WeatherType.Cloudy),
    )
    val weaklyForecast = mapOf<Int, Weather11>(
        0 to Weather11(24, WeatherType.Cloudy),
        2 to Weather11(24, WeatherType.Cloudy),
        4 to Weather11(24, WeatherType.Cloudy),
        6 to Weather11(24, WeatherType.Cloudy),
        8 to Weather11(24, WeatherType.Cloudy),
        10 to Weather11(24, WeatherType.Cloudy),
        12 to Weather11(24, WeatherType.Cloudy),
    )
}

enum class WeatherType {
    Sunny,
    Cloudy,
    Rainy,
    Snowy
}

fun WeatherType.toEmoji(): String {
    return when (this) {
        WeatherType.Sunny -> "☀️"
        WeatherType.Cloudy ->  "☁️️"
        WeatherType.Rainy ->  "🌧️"
        WeatherType.Snowy -> "🌨️"
    }
}

private const val TIME_OUT = 60_000

private val ktorHttpClient = HttpClient(Android) {

    install(JsonFeature) {
        serializer = KotlinxSerializer(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })

        engine {
            connectTimeout = TIME_OUT
            socketTimeout = TIME_OUT
        }
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.v("Logger Ktor =>", message)
            }

        }
        level = LogLevel.ALL
    }

    install(ResponseObserver) {
        onResponse { response ->
            Log.d("HTTP status:", "${response.status.value}")
        }
    }

    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
}

fun ForecastDTO.toForecastVM(): List<WeatherPredicationVM> {
    return this.data.map {
        WeatherPredicationVM(
            date = LocalDateTime.ofInstant(Instant.ofEpochSecond(it.dateTicks), ZoneId.systemDefault()),
            temperature = Temperature.fromKelvin(it.temperaturePressure.temperatureK),
            pressureBar = it.temperaturePressure.pressure.toFloat(),
            weatherType = WeatherType.Sunny,
            wind = Wind(it.wind.speedMS, it.wind.deg),
        )
    }.toList()

}

class Temperature private constructor(private val temperatureK: Float): Comparable<Temperature> {
    companion object {
        fun fromKelvin(temperatureK: Float): Temperature {
            return Temperature(temperatureK)
        }
    }

    val kelvin: Float
        get() {
            return temperatureK;
        }

    val celsius: Float
        get() {
            return temperatureK - 272.15F
        }
    val fahrenheit: Float
        get() {
            return 9 * (temperatureK - 273.15f) / 5f + 32f
        }
    override fun compareTo(other: Temperature): Int {
        return temperatureK.compareTo(other.temperatureK)
    }
}
data class WeatherPredicationVM(
    val date: LocalDateTime,
    val temperature: Temperature,
    val pressureBar: Float,
    val weatherType: WeatherType,
    val wind: Wind,
)

data class Wind(val speedMS: Float, val directionDeg: Int)

@Serializable
data class ForecastDTO(
    @SerialName("cod")
    val code: String,
    @SerialName("list")
    val data: List<WeatherDTO>
)

@Serializable
data class WeatherDTO( // Have used the name from the API need to figure out smthng better
    @SerialName("dt")
    val dateTicks: Long,
    @SerialName("main")
    val temperaturePressure: TemperaturePressureDTO,
    @SerialName("weather")
    val weatherType: List<WeatherTypeDTO>,
    @SerialName("wind")
    val wind: WindDTO,
)

@Serializable
data class TemperaturePressureDTO (
    @SerialName("temp")
    val temperatureK: Float,
    @SerialName("pressure")
    val pressure: Int
)

@Serializable
data class WeatherTypeDTO (
    @SerialName("main")
    val name: String,
    @SerialName("description")
    val description: String
)

@Serializable
data class WindDTO(
    @SerialName("speed")
    val speedMS: Float,
    @SerialName("deg")
    val deg: Int
)