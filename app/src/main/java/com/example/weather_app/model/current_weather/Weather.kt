package com.example.weather_app.model.current_weather

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)