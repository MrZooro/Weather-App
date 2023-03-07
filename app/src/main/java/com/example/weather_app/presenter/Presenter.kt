package com.example.weather_app.presenter

import android.util.Log
import com.example.weather_app.model.WeatherAPIService
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import com.example.weather_app.model.forecast_weather.ForecastWeatherItem
import com.example.weather_app.model.ipGeolocationService
import com.example.weather_app.model.ip_geolocation.ipGeolocation
import com.example.weather_app.view.UpdateView
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.MonthDay
import java.util.*
import kotlin.collections.List

class Presenter(view: UpdateView, weatherAPIKey: String) {
    private var currentWeather : CurrentWeather? = null
    private val weatherAPIService: WeatherAPIService = WeatherAPIService(this, weatherAPIKey)
    private val geolocationService: ipGeolocationService = ipGeolocationService(this)
    private var geolocation: ipGeolocation? = null
    private var viewForUpdate: UpdateView = view

    private var forecastWeather: ForecastWeather? = null

    private val dateFormat : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private lateinit var forecastWeatherByDay: MutableList<List<ForecastWeatherItem>>
    private lateinit var forecastCurWeather: List<ForecastWeatherItem>

    fun updateWeather(lat : Double, lon : Double, units : String, lang: String) {
        weatherAPIService.getCurrentWeather(lat, lon, units, lang)
        weatherAPIService.getForecastWeather(lat, lon, units)
    }

    fun setCurrentWeather(NEWcurrentWeather: CurrentWeather?, code : Int){
        currentWeather = NEWcurrentWeather
        viewForUpdate.UpdateCurrentWeather(code)
    }

    fun setForecastWeather(NEWforecastWeather: ForecastWeather?, code: Int) {
        forecastWeather = NEWforecastWeather

        if(NEWforecastWeather != null) {
            val calendar = Calendar.getInstance()
            var dateStr = dateFormat.format(calendar.time)
            var indexFirstElem = 0
            forecastWeatherByDay = mutableListOf()


            var curDate = true
            var lastElem = 0

            for(i in 0 until NEWforecastWeather.list.size) {
                val tempDateStr = NEWforecastWeather.list[i].dt_txt.split(" ")

                if(tempDateStr[0] != dateStr) {
                    val tempList = NEWforecastWeather.list.subList(indexFirstElem, i)
                    lastElem = i-1
                    if(curDate) {
                        forecastCurWeather = tempList
                    } else {
                        forecastWeatherByDay.add(tempList)
                    }


                    indexFirstElem = i
                    calendar.add(Calendar.DATE, 1)
                    curDate = false
                    dateStr = dateFormat.format(calendar.time)
                }
            }

            if(lastElem != NEWforecastWeather.list.size) {
                val tempList = NEWforecastWeather.list.subList(indexFirstElem, NEWforecastWeather.list.size)
                forecastWeatherByDay.add(tempList)
            }
        }

        viewForUpdate.UpdateForecastWeather(code)
    }

    fun getForecastListWeatherByDay() : List<List<ForecastWeatherItem>> {
        return forecastWeatherByDay
    }

    fun getCurrentWeather() : CurrentWeather? {
        return currentWeather
    }

    fun updateGeolocation(lang: String) {
        geolocationService.getGeolocation(lang)
    }

    fun sendGeolocation(data: ipGeolocation?, code: Int) {
        geolocation = data
        viewForUpdate.UpdateGeolocation(geolocation, code)
    }

    fun getListByDay(day: String, mounth: String, year: String) : List<ForecastWeatherItem>? {

        if(!this::forecastWeatherByDay.isInitialized) {
            return null
        }

        val dateStr = "$year-$mounth-$day"
        var tempStr: String


        tempStr = forecastCurWeather[0].dt_txt.split(" ")[0]
        if(tempStr == dateStr) return forecastCurWeather

        for(i in 0 until forecastWeatherByDay.size) {
            tempStr = forecastWeatherByDay[i][0].dt_txt.split(" ")[0]

            if(tempStr == dateStr) {
                return forecastWeatherByDay[i]
            }
        }

        return null
    }


}