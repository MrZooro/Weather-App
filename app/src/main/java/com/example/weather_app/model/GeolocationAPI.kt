package com.example.weather_app.model


import com.example.weather_app.model.ip_geolocation.ipGeolocation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface GeolocationAPI {
    @GET
    fun getGeolocation(@Url url: String) : Call<ipGeolocation>
}