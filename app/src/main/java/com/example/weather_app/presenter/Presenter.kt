package com.example.weather_app.presenter

import android.util.Log
import com.example.weather_app.model.WeatherAPIService
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import com.example.weather_app.model.ipGeolocationService
import com.example.weather_app.model.ip_geolocation.ipGeolocation
import com.example.weather_app.view.UpdateView

class Presenter(view: UpdateView) {
    private var currentWeather : CurrentWeather? = null
    private lateinit var weatherAPIService: WeatherAPIService
    private lateinit var GeolocationService: ipGeolocationService
    private var geolocation: ipGeolocation? = null
    private var viewForUpdate: UpdateView = view

    private var forecastWeather: ForecastWeather? = null

    fun updateWeather(city : String, apiKey : String) {
        weatherAPIService = WeatherAPIService(this, apiKey)

        weatherAPIService.getCurrentWeather(city)
        weatherAPIService.getForecastWeather(city)
    }

    fun setCurrentWeather(NEWcurrentWeather: CurrentWeather?, code : Int){
        currentWeather = NEWcurrentWeather
        viewForUpdate.UpdateCurrentWeather(currentWeather, code)
    }

    fun setForecastWeather(NEWforecastWeather: ForecastWeather?, code: Int) {
        forecastWeather = NEWforecastWeather
        viewForUpdate.UpdateForecastWeather(forecastWeather, code)
    }

    fun updateGeolocation() {
        GeolocationService = ipGeolocationService(this)
        GeolocationService.getGeolocation()
    }

    fun sendGeolocation(data: ipGeolocation?, code: Int) {
        geolocation = data
        viewForUpdate.UpdateGeolocation(geolocation, code)
    }


}