package com.example.weather_app.view

import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import com.example.weather_app.model.ip_geolocation.ipGeolocation

interface UpdateView {
    fun UpdateCurrentWeather(NEWcurrentWeather: CurrentWeather?, code : Int)
    fun UpdateForecastWeather(NEWforecastWeather: ForecastWeather?, code : Int)
    fun UpdateGeolocation(NEWgeolocation : ipGeolocation?, code: Int)
}

interface DialogResult{
    fun DialogResults(code: Int)
}