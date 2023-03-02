package com.example.weather_app.model

import android.util.Log
import com.example.weather_app.model.ip_geolocation.ipGeolocation
import com.example.weather_app.presenter.Presenter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ipGeolocationService(presenter : Presenter) {
    private val MainPresenter = presenter
    private val BaseURL = "http://ip-api.com/json/"
    private val localization = "?lang=en"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BaseURL + localization)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeolocationAPI::class.java)

    fun getGeolocation(lang: String) {
        val tempURL = "$BaseURL?lang=$lang"
        val call = retrofit.getGeolocation(tempURL)
        call.enqueue(object : Callback<ipGeolocation> {
            override fun onResponse(call: Call<ipGeolocation>, response: Response<ipGeolocation>) {
                if (response.isSuccessful) {
                    response.body()?.let { sendGeolocation(it, 200) }
                    Log.i("OK", "Geolocation: getGeolocation")
                } else {
                    Log.w("Warning", "Geolocation: Something went wrong: " + response.code())
                    sendGeolocation(null, 300)
                }
            }

            override fun onFailure(call: Call<ipGeolocation>, t: Throwable) {
                Log.w("Warning", "Geolocation: Something really went wrong: " + t.message)
                sendGeolocation(null, 400)
            }

        })

    }

    private fun sendGeolocation(data : ipGeolocation?, code : Int) {
        MainPresenter.sendGeolocation(data, code)
    }
}
