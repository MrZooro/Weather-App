package com.example.weather_app.model.forecast_weather

import com.example.weather_app.model.current_weather.*

data class ForecastWeather(
    val cod : String,
    val message : Int,
    val cnt : Int,
    val list: List<ForecastWeatherItem>,
    val city: City
)

data class ForecastWeatherItem(
    val dt : Int,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain : Rain,
    val sys: Sys,
    val dt_txt : String
)

data class Rain(
    val `3h`: Double
)

data class City(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)