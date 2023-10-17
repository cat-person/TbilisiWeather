package cafe.serenity.tbilisiweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import cafe.serenity.tbilisiweather.ui.theme.TbilisiWeatherTheme
import java.time.format.DateTimeFormatter

val formatter = DateTimeFormatter.ofPattern("HH:mm")!!

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            WeatherDB::class.java,
            "contacts.db"
        ).build()
    }

    private val openWeatherRemote by lazy {
        OpenWeatherRemote(BuildConfig.OPEN_WEATHER_API_KEY)
    }

    private val vm by viewModels<WeatherVM>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WeatherVM(db.dao, openWeatherRemote) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TbilisiWeatherTheme {
                val state = vm.state.collectAsState()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.hsl(190f, 0.5f, 0.7f, 1f)
                ) {
                    state.value.let {
                        when (it) {
                            is State.Some -> {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CurrentWeatherWidget(it.currentWeather, Modifier.fillMaxWidth())
                                    WeeklyWeatherWidget(
                                        it.dailyForecast,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            is State.None -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        trackColor = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }

                            is State.Error -> {

                            }
                        }

                    }

                }

                DisposableEffect(vm) {
                    vm.invalidate()
                    onDispose {
                        // vm suspend all the stuff
                    }
                }
            }
        }
    }
}


@Composable
fun CurrentWeatherWidget(weather: DataState<WeatherVMO>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.height(160.dp)) {
        Text(
            text = "Tbilisi",
            fontSize = 48.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )
        when (weather) {
            is DataState.None -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(32.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            }

            is DataState.OutOfDate -> {
                Text(
                    text = "${weather.value.weatherEmoji} ${"%.1f".format(weather.value.temperature.celsius)}°",
                    fontSize = 64.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                )
            }

            is DataState.Synced -> {
                Text(
                    text = "${weather.value.weatherEmoji} ${"%.1f".format(weather.value.temperature.celsius)}°",
                    fontSize = 64.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
fun WeeklyWeatherWidget(forecast: DataState<List<WeatherVMO>>, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        when (forecast) {
            is DataState.None -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            }

            is DataState.OutOfDate -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(forecast.value.size) { itemIdx ->
                        val weather = forecast.value[itemIdx]

                        Row {
                            Text(
                                text = weather.date.format(formatter),
                                modifier = Modifier.padding(16.dp),
                            )
                            Text(
                                text = "${weather.weatherEmoji} ${weather.temperature.celsius}C°",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        Divider(color = Color.White)
                    }
                }
            }

            is DataState.Synced -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(forecast.value.size) { itemIdx ->
                        val weather = forecast.value[itemIdx]

                        Row {
                            Text(
                                text = weather.date.format(formatter),
                                modifier = Modifier.padding(16.dp),
                            )
                            Text(
                                text = "${weather.weatherEmoji} ${weather.temperature.celsius}C°",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        Divider(color = Color.White)
                    }
                }
            }
        }
    }
}