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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import org.koin.androidx.scope.ScopeActivity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val httpClient = HttpClient()
//
//        MainScope().launch {
//            val response = ktorHttpClient.get<ForecastResponse>("https://api.openweathermap.org/data/2.5/weather?lat=41.69411&lon=44.83368&appid=a58c4682a49e63fb623ba14dce0dd7b4")
//            Log.w("MEOOOOW", "${response}")
//        }

        setContent {
            TbilisiWeatherTheme {
                val forecast = Forecast()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.hsl(200f, 0.4f, 0.7f, 1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CurrentWeatherWidget(forecast.currentWeather, Modifier.fillMaxWidth())
                        TodayWeatherWidget(forecast.todayForecast)
                        WeaklyWeatherWidget(forecast.weaklyForecast)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherWidget(weather: Weather, modifier: Modifier = Modifier) {
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
fun TodayWeatherWidget(todayWeather: Map<Int, Weather>, modifier: Modifier = Modifier) {

    Card(modifier = modifier) {
        LazyRow(modifier = modifier) {

            val keys = todayWeather.keys.toIntArray()
            items(keys.size) { itemIdx ->
                val key = keys[itemIdx]
                val weather = todayWeather.get(key)
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

            }
        }
    }
}


@Composable
fun WeaklyWeatherWidget(weaklyWeather: Map<Int, Weather>, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            val keys = weaklyWeather.keys.toIntArray()

            items(keys.size) { itemIdx ->

                val key = keys[itemIdx]
                val weather = weaklyWeather.get(key)

                Row {
                    Text(
                        text = "$key",
                        modifier = Modifier.padding(16.dp),
                    )
                    Text(
                        text = "${weather?.weatherType?.toEmoji()} ${weather?.temperatureC}°",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Divider(color = Color.White)

            }
        }
    }
}

class Weather(val temperatureC: Int, val weatherType: WeatherType)

class Forecast {
    val currentWeather = Weather(24, WeatherType.Cloudy)
    val todayForecast = mapOf<Int, Weather>(
        0 to Weather(24, WeatherType.Cloudy),
        2 to Weather(24, WeatherType.Sunny),
        4 to Weather(24, WeatherType.Cloudy),
        6 to Weather(24, WeatherType.Rainy),
        8 to Weather(24, WeatherType.Cloudy),
        10 to Weather(24, WeatherType.Snowy),
        12 to Weather(24, WeatherType.Cloudy),
    )
    val weaklyForecast = mapOf<Int, Weather>(
        0 to Weather(24, WeatherType.Cloudy),
        2 to Weather(24, WeatherType.Cloudy),
        4 to Weather(24, WeatherType.Cloudy),
        6 to Weather(24, WeatherType.Cloudy),
        8 to Weather(24, WeatherType.Cloudy),
        10 to Weather(24, WeatherType.Cloudy),
        12 to Weather(24, WeatherType.Cloudy),
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

//private const val TIME_OUT = 60_000

//private val ktorHttpClient = HttpClient(Android) {
//
//    install(JsonFeature) {
//        serializer = KotlinxSerializer(Json {
//            prettyPrint = true
//            isLenient = true
//            ignoreUnknownKeys = true
//        })
//
//        engine {
//            connectTimeout = TIME_OUT
//            socketTimeout = TIME_OUT
//        }
//    }
//
//    install(Logging) {
//        logger = object : Logger {
//            override fun log(message: String) {
//                Log.v("Logger Ktor =>", message)
//            }
//
//        }
//        level = LogLevel.ALL
//    }
//
//    install(ResponseObserver) {
//        onResponse { response ->
//            Log.d("HTTP status:", "${response.status.value}")
//        }
//    }
//
//    install(DefaultRequest) {
//        header(HttpHeaders.ContentType, ContentType.Application.Json)
//    }
//}

@Serializable
data class ForecastResponse(
    @SerialName("id")
    val id: String,
)