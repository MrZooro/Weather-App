package com.example.weather_app.model.forecast_weather

import com.example.weather_app.model.current_weather.CurrentWeather
import com.google.gson.annotations.SerializedName

data class ForecastWeather(
    @SerializedName("list") val list: List<CurrentWeather>
)
