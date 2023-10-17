package cafe.serenity.tbilisiweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class WeatherVM(
    private val dao: WeatherDao,
    private val remote: OpenWeatherRemote
) : ViewModel() {
    private val _currentWeatherFlow = MutableStateFlow<DataState<WeatherVMO>>(DataState.None())
    private val _dailyForecastFlow = MutableStateFlow<DataState<List<WeatherVMO>>>(DataState.None())
    private val _errorFlow = MutableStateFlow(Pair(200, ""))

    val state = combine(
        _currentWeatherFlow,
        _dailyForecastFlow,
        _errorFlow
    ) { currentWeatherState, dailyForecast, error ->
        if (currentWeatherState is DataState.None && dailyForecast is DataState.None) {
            if (_errorFlow.value.first == 200) {
                State.None()
            } else {
                State.Error(_errorFlow.value.first, _errorFlow.value.second)
            }
        } else {
            State.Some(
                currentWeather = currentWeatherState,
                dailyForecast = dailyForecast,
                errorMessage = error.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State.None())

    private fun onEvent(event: Event) {
        when (event) {
            is Event.UpsertCurrent -> {
                viewModelScope.launch {
                    _currentWeatherFlow.update {
                        event.dataPoint
                    }
                }
            }

            is Event.AddPredictionBatch -> {
                _dailyForecastFlow.update {
                    event.dataPoints
                }
            }

            is Event.NetworkError -> {
                _errorFlow.update {
                    it
                }
            }
        }
    }

    fun invalidate() {
        invalidateCurrent()
        invalidateDailyForecast()
    }

    private fun invalidateCurrent() {
        viewModelScope.launch {
            val lastSaved = dao.getLastSaved()
            if (lastSaved != null) {
                if (LocalDateTime.now().minusHours(1)
                        .toEpochSecond(ZoneOffset.UTC) < lastSaved.dateSecondsFromEpoch
                ) {
                    onEvent(Event.UpsertCurrent(DataState.OutOfDate(lastSaved.toWeatherVMO())))
                    syncCurrentWithRemote()
                } else {
                    onEvent(Event.UpsertCurrent(DataState.Synced(lastSaved.toWeatherVMO())))
                }
            } else {
                onEvent(Event.UpsertCurrent(DataState.None()))
                syncCurrentWithRemote()
            }
        }
    }

    private fun invalidateDailyForecast() {
        viewModelScope.launch {
            // Cashing
            syncDailyForecastWithRemote()
        }
    }

    private suspend fun syncCurrentWithRemote() {
        remote.getCurrentWeather().let {
            when (it) {
                is OpenWeatherRemote.Result.Success -> onEvent(
                    Event.UpsertCurrent(
                        DataState.Synced(it.value.toWeatherDBO().toWeatherVMO())
                    )
                )

                is OpenWeatherRemote.Result.Failure -> {
                    onEvent(Event.NetworkError(it.statusCode.value, "${it.message}"))
                }
            }
        }
    }

    private suspend fun syncDailyForecastWithRemote() {
        remote.getDailyForecast().let { dailyForecast ->
            when (dailyForecast) {
                is OpenWeatherRemote.Result.Success -> onEvent(
                    Event.AddPredictionBatch(
                        DataState.Synced(dailyForecast.value.data.map {
                            it.toWeatherDBO().toWeatherVMO()
                        })
                    )
                )

                is OpenWeatherRemote.Result.Failure -> {
                    onEvent(
                        Event.NetworkError(
                            dailyForecast.statusCode.value,
                            "${dailyForecast.message}"
                        )
                    )
                }
            }
        }
    }

    sealed interface Event {
        data class UpsertCurrent(val dataPoint: DataState<WeatherVMO>) : Event
        data class AddPredictionBatch(val dataPoints: DataState<List<WeatherVMO>>) : Event
        data class NetworkError(val errorCode: Int, val message: String) : Event
    }
}

sealed interface DataState<T> {
    class None<T> : DataState<T> // Local data is not available
    class OutOfDate<T>(val value: T) : DataState<T>
    class Synced<T>(val value: T) : DataState<T>
}


interface State {
    data class Some(
        var currentWeather: DataState<WeatherVMO> = DataState.None(),
        val dailyForecast: DataState<List<WeatherVMO>> = DataState.None(),
        val errorMessage: String? = null
    ) : State

    class None : State
    class Error(val status: Int, val message: String) : State
}

class Temperature private constructor(private val temperatureK: Float) : Comparable<Temperature> {
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

fun WeatherVMO.toWeatherDBO(): WeatherDBO {
    return WeatherDBO(
        dateSecondsFromEpoch = this.date.toEpochSecond(ZoneOffset.UTC),
        temperatureK = this.temperature.kelvin,
        weatherCode = this.weatherCode
    )
}

fun WeatherDBO.toWeatherVMO(): WeatherVMO {
    return WeatherVMO(
        date = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(this.dateSecondsFromEpoch),
            ZoneId.systemDefault()
        ),
        temperature = Temperature.fromKelvin(this.temperatureK),
        pressureBar = 1000f,
        weatherCode = 800,
        wind = WindVMO(10f, 42),
    )
}

fun WeatherDTO.toWeatherDBO(): WeatherDBO {
    return WeatherDBO(
        dateSecondsFromEpoch = this.dateTicks, // Wrong
        temperatureK = this.temperaturePressure.temperatureK,
        weatherCode = this.weatherType.map { it.id }.firstOrNull() ?: -1
    )
}

data class WeatherVMO(
    val date: LocalDateTime,
    val temperature: Temperature,
    val pressureBar: Float,
    val weatherCode: Int,
    val wind: WindVMO
) {

    val weatherEmoji: String
        get() = when (weatherCode) {
            200, 201, 202, 210, 211, 212, 221, 230, 231, 232 -> "⛈️" // Actually it's Thunderstorm
            300, 301, 302, 310, 311, 312, 313, 314, 321 -> "🌦️" // Actually it's Drizzle
            500, 501, 502, 503, 504, 511, 520, 521, 522, 531 -> "🌧"
            600, 601, 602, 611, 612, 613, 615, 616, 620, 621, 622 -> "🌨️"
            701 -> "🌫️" // Mist
            711 -> "🌫️" // Smoke
            721 -> "🌫️" // Haze
            731 -> "🫣"
            741 -> "🌫️" // Fog
            751 -> "🫣" // Sand
            761 -> "🫣" // Dust
            762 -> "🫣" // Ash	volcanic
            771 -> "🌩️" // Squall
            781 -> "🌪" // Tornado
            800 -> "☀️"
            801, 802, 803, 804 -> "☁️"
            else -> "🫣"
        }

    companion object {
        val default = WeatherVMO(
            date = LocalDateTime.now(),
            temperature = Temperature.fromKelvin(0.0f),
            pressureBar = 1000f,
            weatherCode = 800,
            wind = WindVMO(0.0f, 0)
        )
    }
}

data class WindVMO(val speedMS: Float, val directionDeg: Int)