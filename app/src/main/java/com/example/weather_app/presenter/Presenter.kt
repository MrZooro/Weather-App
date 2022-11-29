package com.example.weather_app.presenter

import android.util.Log
import com.example.weather_app.model.WeatherAPIService
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import com.example.weather_app.model.ipGeolocationService
import com.example.weather_app.model.ip_geolocation.ipGeolocation
import com.example.weather_app.view.UpdateView

class Presenter(view: UpdateView) {
    private lateinit var currentWeather : CurrentWeather
    private lateinit var weatherAPIService: WeatherAPIService
    private lateinit var GeolocationService: ipGeolocationService
    private lateinit var geolocation: ipGeolocation
    private var viewForUpdate: UpdateView = view

    private lateinit var forecastWeather: ForecastWeather

    fun updateWeather(city : String, apiKey : String) {
        weatherAPIService = WeatherAPIService(this, apiKey)

        weatherAPIService.getCurrentWeather(city)
        weatherAPIService.getForecastWeather(city)
    }

    fun setCurrentWeather(NEWcurrentWeather: CurrentWeather){
        currentWeather = NEWcurrentWeather
        viewForUpdate.UpdateCurrentWeather(currentWeather)
    }

    fun setForecastWeather(NEWforecastWeather: ForecastWeather) {
        forecastWeather = NEWforecastWeather
        viewForUpdate.UpdateForecastWeather(forecastWeather)
    }

    fun updateGeolocation() {
        GeolocationService = ipGeolocationService(this)
        GeolocationService.getGeolocation()
    }

    fun sendGeolocation(data: ipGeolocation) {
        geolocation = data
        viewForUpdate.UpdateGeolocation(geolocation)
    }


}