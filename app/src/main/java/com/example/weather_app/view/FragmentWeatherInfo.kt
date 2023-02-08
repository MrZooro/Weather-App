package com.example.weather_app.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.weather_app.R
import com.example.weather_app.model.current_weather.CurrentWeather
import com.example.weather_app.model.forecast_weather.ForecastWeatherItem
import com.example.weather_app.model.forecast_weather.Rain
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class FragmentWeatherInfo(private val weatherList : List<ForecastWeatherItem>,
                          private val currentWeather: CurrentWeather?,
                          private val context : MainActivity): BottomSheetDialogFragment() {

    private val dateFormat : SimpleDateFormat = SimpleDateFormat("EEEE\ndd/MM/yyyy", Locale.US)
    private val hoursMinutes : SimpleDateFormat = SimpleDateFormat("hh:mm", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_weather_info, container, false)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var mainView : View
    private lateinit var timeList : List<String>
    private lateinit var newCurrentWeather: ForecastWeatherItem
    private lateinit var newWeatherList : List<ForecastWeatherItem>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainView = view

        if(currentWeather != null) {
            val tempForecastWeather = curWeatherToForecast()
            if(tempForecastWeather != null) {
                newCurrentWeather = tempForecastWeather
                timeList = createTimeListWithCur()
            } else {
                Log.e("Error", "FWI: couldn't parse CurrentWeather in ForecastWeatherItem")
                return
            }
        } else {
            timeList = createTimeList()
            newWeatherList = weatherList
        }


        addBigWeatherCards()
        changeTextViews()
        addWindSpeedCards()
        addPopCards()
    }

    private fun curWeatherToForecast() : ForecastWeatherItem? {
        if(currentWeather != null) {
            return ForecastWeatherItem(
                currentWeather.dt,
                currentWeather.main,
                currentWeather.weather,
                currentWeather.clouds,
                currentWeather.wind,
                currentWeather.visibility,
                -1.0,
                Rain(-1.0),
                currentWeather.sys,
                "-1.0"
            )
        }
        return null
    }

    private fun createTimeList() : List<String> {
        var dateStr : String
        var timeStr : String


        val mutableList: MutableList<String> = mutableListOf()
        for(i in weatherList.indices) {
            dateStr = weatherList[i].dt_txt
            timeStr = dateStr.substring(dateStr.length-8, dateStr.length-3)
            mutableList.add(timeStr)
        }
        return mutableList
    }

    private fun createTimeListWithCur() : List<String> {

        var timeStr = hoursMinutes.format(Calendar.getInstance().time)
        val curDateInt = timeStr.substring(0, 2).toInt()
        var checked = false
        var dateStr : String
        var tempDateInt : Int

        val mutableList: MutableList<String> = mutableListOf()
        mutableList.add(timeStr)

        val mutNewWeatherList : MutableList<ForecastWeatherItem> = mutableListOf()
        mutNewWeatherList.add(newCurrentWeather)

        for(i in weatherList.indices) {
            dateStr = weatherList[i].dt_txt
            timeStr = dateStr.substring(dateStr.length-8, dateStr.length-3)

            if(!checked) {
                tempDateInt = timeStr.substring(0, 2).toInt()
                if(tempDateInt > curDateInt) {
                    checked = true
                    mutableList.add(timeStr)
                    mutNewWeatherList.add(weatherList[i])
                }
            } else {
                mutableList.add(timeStr)
                mutNewWeatherList.add(weatherList[i])
            }
        }

        newWeatherList = mutNewWeatherList
        return mutableList
    }

    @SuppressLint("SetTextI18n")
    private fun addBigWeatherCards() {
        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 140 * ds.density
        val layoutWidth = 88 * ds.density

        val defaultMargin = 96 * ds.density
        var layoutMargin = 18 * ds.density

        for (i in newWeatherList.indices) {
            val layer : View
            val constraintLayout = mainView.findViewById<ConstraintLayout>(R.id.fwi_place_for_big_weather_cards)
            layer = layoutInflater.inflate(R.layout.big_weather_card, constraintLayout, false)

            val params : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                ceil(layoutWidth).toInt(),
                ceil(layoutHeight).toInt())

            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = ceil(layoutMargin).toInt()

            if(i == weatherList.size-1) {
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = ceil(18 * ds.density).toInt()
            }
            layer.layoutParams = params
            layoutMargin += defaultMargin

            layer.findViewById<TextView>(R.id.big_card_time_tv).text = timeList[i]

            layer.findViewById<View>(R.id.big_card_weather_icon).background =
                context.SMALLweatherStateImage[weatherList[i].weather[0].main]?.let {
                    getDrawable(context, it)
                }

            layer.findViewById<TextView>(R.id.big_card_weather_description_tv).text =
                weatherList[i].weather[0].description

            layer.findViewById<TextView>(R.id.big_card_temperature_tv).text =
                getTemperatureStr(weatherList[i].main.temp)

            val tempTextView = layer.findViewById<TextView>(R.id.big_card_feels_like_tv)
            tempTextView.text = tempTextView.text.toString() +
                    getTemperatureStr(weatherList[i].main.feels_like) + "°C"

            constraintLayout?.addView(layer)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeTextViews() {
        if(newWeatherList.isEmpty()) return

        var atmoPressure = 0.0
        var humidity = 0.0
        var clouds = 0.0
        var visibility = 0.0

        for(i in weatherList.indices) {
            atmoPressure += weatherList[i].main.pressure
            humidity += weatherList[i].main.humidity
            clouds += weatherList[i].clouds.all
            visibility += weatherList[0].visibility
        }

        val decimal = weatherList.size
        atmoPressure = (atmoPressure / decimal)
        humidity = (humidity / decimal)
        clouds = (clouds / decimal)
        visibility = (visibility / decimal)

        var tempTV = mainView.findViewById<TextView>(R.id.fwi_atmospheric_pressure_tv)
        tempTV.text = tempTV.text.toString() + atmoPressure.roundToInt() + " mmHg"

        tempTV = mainView.findViewById(R.id.fwi_humidity_tv)
        tempTV.text = tempTV.text.toString() + humidity.roundToInt() + "%"

        tempTV = mainView.findViewById(R.id.fwi_cloud_cover_tv)
        tempTV.text = tempTV.text.toString() + clouds.roundToInt() + "%"

        tempTV = mainView.findViewById(R.id.fwi_visibility_tv)
        tempTV.text = tempTV.text.toString() + visibility.roundToInt() + " m"

        val calendar = Calendar.getInstance()
        val dateStr = weatherList[0].dt_txt.split(" ")[0].split("-")
        calendar.set(Calendar.YEAR, dateStr[0].toInt())
        calendar.set(Calendar.MONTH, dateStr[1].toInt())
        calendar.set(Calendar.DAY_OF_MONTH, dateStr[2].toInt())
        val tempDateString = dateFormat.format(calendar.time)
        mainView.findViewById<TextView>(R.id.fwi_date_tv).text = tempDateString
    }

    @SuppressLint("SetTextI18n")
    private fun addWindSpeedCards() {
        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 50 * ds.density
        val layoutWidth = 74 * ds.density

        val defaultMargin = 78 * ds.density
        var layoutMargin = 18 * ds.density

        for (i in newWeatherList.indices) {
            val layer : View
            val constraintLayout = mainView.findViewById<ConstraintLayout>(R.id.fwi_place_for_wind_speed_cards)
            layer = layoutInflater.inflate(R.layout.small_data_card, constraintLayout, false)

            val params : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                ceil(layoutWidth).toInt(),
                ceil(layoutHeight).toInt())

            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = ceil(layoutMargin).toInt()

            if(i == weatherList.size-1) {
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = ceil(18 * ds.density).toInt()
            }
            layer.layoutParams = params
            layoutMargin += defaultMargin

            layer.findViewById<TextView>(R.id.data_card_time_tv).text = timeList[i]

            layer.findViewById<TextView>(R.id.data_card_info_tv).text =
                weatherList[i].wind.speed.roundToInt().toString() + " m/sec"

            constraintLayout?.addView(layer)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addPopCards() {
        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 50 * ds.density
        val layoutWidth = 74 * ds.density

        val defaultMargin = 78 * ds.density
        var layoutMargin = 18 * ds.density

        for (i in newWeatherList.indices) {
            val layer : View
            val constraintLayout = mainView.findViewById<ConstraintLayout>(R.id.fwi_place_for_chance_of_rain_cards)
            layer = layoutInflater.inflate(R.layout.small_data_card, constraintLayout, false)

            val params : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                ceil(layoutWidth).toInt(),
                ceil(layoutHeight).toInt())

            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = ceil(layoutMargin).toInt()

            if(i == weatherList.size-1) {
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = ceil(18 * ds.density).toInt()
            }
            layer.layoutParams = params
            layoutMargin += defaultMargin

            layer.findViewById<TextView>(R.id.data_card_time_tv).text = timeList[i]

            layer.findViewById<TextView>(R.id.data_card_info_tv).text =
                (weatherList[i].pop * 100).toString() + "%"

            constraintLayout?.addView(layer)
        }
    }

    private fun getTemperatureStr(temperature: Double) : String{

        val roundTemp : Int = (temperature - 273.15).roundToInt()

        return if(roundTemp > 0) {
            "+$roundTemp"
        } else if(roundTemp < 0) {
            "$roundTemp"
        } else "0"
    }

    override fun onDestroy() {
        super.onDestroy()
        context.startBSDF = false
    }
}