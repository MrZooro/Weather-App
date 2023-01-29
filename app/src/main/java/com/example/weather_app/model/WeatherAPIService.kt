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
    private var URLcurWeather : String = "https://api.openweathermap.org/data/2.5/weather?"
    private var URLforWeather : String = "https://api.openweathermap.org/data/2.5/forecast?"
    private val MainApiKey : String = apiKey
    private val MainPresener = presenter
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherAPI::class.java)

    fun getCurrentWeather(lat : Double, lon : Double) {
        URLcurWeather = "${URLcurWeather}lat=$lat&lon=$lon&appid=$MainApiKey"


        val call = retrofit.getDataCurrentWeather(URLcurWeather)
        call.enqueue(object : Callback<CurrentWeather>{
            override fun onResponse(
                call: retrofit2.Call<CurrentWeather>,
                response: Response<CurrentWeather>
            ) {
                if(response.isSuccessful) {
                    response.body()?.let { sendCurrentWeather(it, 200) }
                    Log.i("OK", "CurrentWeather: getCurrentWeather")
                } else {
                    Log.w("Warning", "CurrentWeather: Something went wrong: " + response.code())
                    sendCurrentWeather(null, 300)
                }
            }

            override fun onFailure(call: retrofit2.Call<CurrentWeather>, t: Throwable) {
                Log.w("Warning", "CurrentWeather: Something really went wrong: " + t.message)
                sendCurrentWeather(null, 400)
            }

        })
    }

    fun sendCurrentWeather(data : CurrentWeather?, code : Int) {
        MainPresener.setCurrentWeather(data, code)
    }

    fun getForecastWeather(lat: Double, lon: Double) {
        URLforWeather = "${URLforWeather}lat=$lat&lon=$lon&appid=$MainApiKey"

        val call = retrofit.getDataForecastWeather(URLforWeather)
        call.enqueue(object : Callback<ForecastWeather>{
            override fun onResponse(
                call: retrofit2.Call<ForecastWeather>,
                response: Response<ForecastWeather>
            ) {
                if(response.isSuccessful) {
                    response.body()?.let { sendForecastWeather(it, 200) }
                    Log.i("OK", "ForecastWeather: getForecastWeather")
                } else {
                    Log.w("Warning", "ForecastWeather: Something went wrong: " + response.code())
                    sendForecastWeather(null, 300)
                }
            }

            override fun onFailure(call: retrofit2.Call<ForecastWeather>, t: Throwable) {
                Log.w("Warning", "ForecastWeather: Something really went wrong: " + t.message)
                sendForecastWeather(null, 400)
            }

        })
    }

    fun sendForecastWeather(data : ForecastWeather?, code: Int) {
        MainPresener.setForecastWeather(data, code)
    }

}
