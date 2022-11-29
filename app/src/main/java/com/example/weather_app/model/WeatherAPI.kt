package com.example.weather_app.model

import retrofit2.Call
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import retrofit2.http.GET
import retrofit2.http.Url

interface WeatherAPI {
    @GET
    fun getDataCurrentWeather(@Url url : String) : Call<CurrentWeather>

    @GET
    fun getDataForecastWeather(@Url url : String) : Call<ForecastWeather>
}