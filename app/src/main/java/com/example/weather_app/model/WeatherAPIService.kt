package com.example.weather_app.model

import android.util.Log
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeather
import com.example.weather_app.presenter.Presenter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherAPIService(presenter: Presenter, apiKey : String) {
    private var URLcurWeather : String = "https://api.openweathermap.org/data/2.5/weather?q="
    private var URLforWeather : String = "https://api.openweathermap.org/data/2.5/forecast?q="
    private val MainApiKey : String = apiKey
    private val MainPresener = presenter
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherAPI::class.java)

    fun getCurrentWeather(city : String) {
        URLcurWeather = "$URLcurWeather$city&appid=$MainApiKey"

        val call = retrofit.getDataCurrentWeather(URLcurWeather)
        call.enqueue(object : Callback<CurrentWeather>{
            override fun onResponse(
                call: retrofit2.Call<CurrentWeather>,
                response: Response<CurrentWeather>
            ) {
                if(response.isSuccessful) {
                    response.body()?.let { sendCurrentWeather(it) }
                    Log.i("OK", "CurrentWeather: getCurrentWeather")
                } else {
                    Log.e("Error", "CurrentWeather: Something went wrong: " + response.code())
                }
            }

            override fun onFailure(call: retrofit2.Call<CurrentWeather>, t: Throwable) {
                Log.e("Error", "CurrentWeather: Something really went wrong: " + t.message)
            }

        })
    }

    fun sendCurrentWeather(data : CurrentWeather) {
        MainPresener.setCurrentWeather(data)
    }

    fun getForecastWeather(city: String) {
        URLforWeather = "$URLforWeather$city&appid=$MainApiKey"

        val call = retrofit.getDataForecastWeather(URLforWeather)
        call.enqueue(object : Callback<ForecastWeather>{
            override fun onResponse(
                call: retrofit2.Call<ForecastWeather>,
                response: Response<ForecastWeather>
            ) {
                if(response.isSuccessful) {
                    response.body()?.let { sendForecastWeather(it) }
                    Log.i("OK", "ForecastWeather: getForecastWeather")
                } else {
                    Log.e("Error", "ForecastWeather: Something went wrong: " + response.code())
                }
            }

            override fun onFailure(call: retrofit2.Call<ForecastWeather>, t: Throwable) {
                Log.e("Error", "ForecastWeather: Something really went wrong: " + t.message)
            }

        })
    }

    fun sendForecastWeather(data : ForecastWeather) {
        MainPresener.setForecastWeather(data)
    }

}
