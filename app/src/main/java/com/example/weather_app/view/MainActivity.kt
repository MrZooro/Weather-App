package com.example.weather_app.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.weather_app.BuildConfig
import com.example.weather_app.R
import com.example.weather_app.databinding.ActivityMainBinding
import com.example.weather_app.model.forecast_weather.ForecastWeatherItem
import com.example.weather_app.model.ip_geolocation.ipGeolocation
import com.example.weather_app.presenter.Presenter
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), View.OnClickListener, UpdateView,
    DialogResult {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: Presenter

    private var city : Place? = null
    private lateinit var date : Calendar

    val ruLocale = Locale("ru")
    val enLocale = Locale("en")
    private var dateFormat : SimpleDateFormat = SimpleDateFormat("EEEE\ndd/MM/yyyy", Locale.US)

    var startBSDF = false

    private var settingsDialogOpen = false
    private var connectionDialogOpen = false

    private var unitsOfMeasurement = "metric"
    private var language = "Russian"
    var langCode = "en"
    private var atmoPressureUnits = "mmHg"

    private lateinit var saveInfo: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saveInfo = getSharedPreferences("save_info", MODE_PRIVATE)

        val tempUnits = saveInfo.getString("unitsOfMeasurement", "metric")
        if(tempUnits != null) {
            unitsOfMeasurement = tempUnits
        }
        val tempLanguage = saveInfo.getString("language", "Russian")
        if(tempLanguage != null) {
            language = tempLanguage
        }

        langCode = if(language[0] == 'E') {
            "en"
        } else {
            "ru"
        }

        presenter = Presenter(this, BuildConfig.OpenWeatherMap_API_KEY)
        binding.refreshLayout.isRefreshing = true
        setAppLocale()

        val tempAtmoPressure = saveInfo.getString("atmoPressureUnits", "mmHg")
        if(tempAtmoPressure != null) {
            atmoPressureUnits = tempAtmoPressure
        }


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.edittextLocation.isFocusable = false
        binding.edittextLocation.setOnClickListener(this)

        binding.backToUserGeolocationButton.setOnClickListener{
            binding.backToUserGeolocationButton.isClickable = false
            binding.backToUserGeolocationButton.visibility = View.GONE
            city = null

            binding.refreshLayout.isRefreshing = true

            presenter.updateGeolocation(langCode)
        }

        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }

        binding.settingsButton.setOnClickListener{
            if(!settingsDialogOpen) {
                settingsDialogOpen = true
                val settings = SettingsFragmentDialog(this, unitsOfMeasurement, language,
                    atmoPressureUnits)

                settings.show(supportFragmentManager, "Something")
            }
        }
    }

    private fun setAppLocale() {
        if(language[0] == 'E') {
            Locale.setDefault(enLocale)
            val config = resources.configuration
            config.setLocale(enLocale)
            resources.updateConfiguration(config, resources.displayMetrics)
            dateFormat = SimpleDateFormat("EEEE\ndd/MM/yyyy", enLocale)

            Places.initialize(applicationContext, BuildConfig.Places_API_KEY, enLocale)
            Places.createClient(this)

            if(city != null) {
                binding.backToUserGeolocationButton.isClickable = false
                binding.backToUserGeolocationButton.visibility = View.GONE
                city = null

                binding.refreshLayout.isRefreshing = true
            }
            presenter.updateGeolocation(langCode)
        } else {
            Locale.setDefault(ruLocale)
            val config = resources.configuration
            config.setLocale(ruLocale)
            resources.updateConfiguration(config, resources.displayMetrics)
            dateFormat = SimpleDateFormat("EEEE\ndd/MM/yyyy", ruLocale)

            Places.initialize(applicationContext, BuildConfig.Places_API_KEY, ruLocale)
            Places.createClient(this)

            if(city != null) {
                binding.backToUserGeolocationButton.isClickable = false
                binding.backToUserGeolocationButton.visibility = View.GONE
                city = null

                binding.refreshLayout.isRefreshing = true
            }
            presenter.updateGeolocation(langCode)
        }
    }

    private fun refresh() {
        if (city != null) {
            val lat = city?.latLng?.latitude
            val lon = city?.latLng?.longitude

            if (lat != null && lon != null) {
                getCur = false
                getFor = false
                presenter.updateWeather(
                    lat,
                    lon,
                    unitsOfMeasurement,
                    langCode
                )
            } else {
                Log.w("Warning", "Places: latitude or longitude is null")
            }
        } else {
            presenter.updateGeolocation(langCode)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun fillScrollView() {
        var layer : View
        val inflater : LayoutInflater = layoutInflater

        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 116 * ds.density
        val layoutWidth = 88 * ds.density
        val defaultMargin = 96 * ds.density
        var layoutMargin = 18 * ds.density

        val tempCalendar = date


        val sceneRoot = binding.forTransition as ViewGroup
        val autoTransition = AutoTransition()
        autoTransition.duration = 50
        autoTransition.interpolator = LinearInterpolator()

        val forecastWeatherByDay = presenter.getForecastListWeatherByDay()

        var element : List<ForecastWeatherItem>
        var minTemp : Double
        var maxTemp : Double
        var iconImportance : Int
        var whatWeatherDescription = ""
        var tempTextColor : Int?
        var tempCardColor : Int?

        for(i in forecastWeatherByDay.indices) {

            element = forecastWeatherByDay[i]
            minTemp = element[0].main.temp_min
            maxTemp = element[0].main.temp_max
            iconImportance = 0

            for(j in 1 until element.size) {
                if(element[j].main.temp_min < minTemp) minTemp = element[j].main.temp_min

                if(element[j].main.temp_max > maxTemp) maxTemp = element[j].main.temp_max

                val tempIconImportance = whatMoreImportant[element[j].weather[0].main]
                if(tempIconImportance != null) {
                    if(tempIconImportance > iconImportance) {
                        whatWeatherDescription = element[j].weather[0].main
                        iconImportance = tempIconImportance
                    }
                }
            }

            layer = inflater.inflate(R.layout.small_weather_card, binding.placeForFutureDays, false)

            val params = ConstraintLayout.LayoutParams(
                ceil(layoutWidth).toInt(),
                ceil(layoutHeight).toInt()
            )

            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = ceil(layoutMargin).toInt()
            if(i == forecastWeatherByDay.size-1) {
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = ceil(18 * ds.density).toInt()
            }

            layer.layoutParams = params

            tempCalendar.add(Calendar.DATE, 1)

            layer.findViewById<TextView>(R.id.card_date).text = dateFormat.format(tempCalendar.time)

            val minTempInt = (minTemp).roundToInt()
            val maxTempInt = (maxTemp).roundToInt()

            var tempStr: String = if (minTempInt < 0) {
                "$minTempInt .. "
            } else if (minTempInt > 0){
                "+$minTempInt .. "
            } else {
                "0 .. "
            }

            tempStr += if(maxTempInt < 0) {
                "$maxTempInt" + getTemperatureMark()
            } else if (maxTempInt > 0) {
                "+$maxTempInt" + getTemperatureMark()
            } else {
                "0" + getTemperatureMark()
            }

            layer.findViewById<TextView>(R.id.card_temperature).text = tempStr

            layer.findViewById<View>(R.id.card_weather_icon).background =
                smallWeatherStateImage[whatWeatherDescription]?.let { getDrawable(it) }

            tempTextColor = textColor[whatWeatherDescription]
            if(tempTextColor != null) {
                layer.findViewById<TextView>(R.id.card_temperature).setTextColor(ContextCompat.getColor(this, tempTextColor))
                layer.findViewById<TextView>(R.id.card_date).setTextColor(ContextCompat.getColor(this, tempTextColor))
            }

            tempCardColor = cardColor[whatWeatherDescription]
            if(tempCardColor != null) {
                layer.findViewById<Button>(R.id.small_weather_card).backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, tempCardColor))
            }

            TransitionManager.beginDelayedTransition(sceneRoot, autoTransition)

            layer.findViewById<Button>(R.id.small_weather_card).setOnClickListener{
                if(startBSDF) {
                    return@setOnClickListener
                }
                startBSDF = true

                val par : ViewGroup = it.parent as ViewGroup
                val tempDateStr = par.findViewById<TextView>(R.id.card_date).text.toString()
                val dateList = tempDateStr.split("\n")[1].split("/")

                val weatherList = presenter.getListByDay(dateList[0], dateList[1], dateList[2])

                if(weatherList != null) {
                    val frag = FragmentWeatherInfo(weatherList, null, this)
                    frag.show(supportFragmentManager, "Something")
                } else {
                    Log.e("Error", "MainActivity: cannot find weather list")
                }
            }


            binding.placeForFutureDays.addView(layer)
            layoutMargin += defaultMargin
        }

        binding.wasteidScrollView.smoothScrollTo(0, 0)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun changeMainCard() {
        val sceneRoot = binding.forTransition as ViewGroup
        val autoTransition = AutoTransition()
        autoTransition.duration = 50
        autoTransition.interpolator = LinearInterpolator()
        TransitionManager.beginDelayedTransition(sceneRoot, autoTransition)

        if(city != null) binding.city.text = city?.name

        val currentWeather = presenter.getCurrentWeather()

        if(currentWeather != null) {
            val tempTextColor = textColor[currentWeather.weather[0].main]
            if(tempTextColor != null) {
                binding.city.setTextColor(ContextCompat.getColor(this, tempTextColor))
                binding.currentTemperature.setTextColor(ContextCompat.getColor(this, tempTextColor))
                binding.currentDate.setTextColor(ContextCompat.getColor(this, tempTextColor))
            }
            val tempCardColor = cardColor[currentWeather.weather[0].main]
            if(tempCardColor != null) {
                binding.currentWeatherInformationBackground.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, tempCardColor))
                window.statusBarColor = ContextCompat.getColor(applicationContext, tempCardColor)
            }

            val tempTemperature = (currentWeather.main.temp).roundToInt()
            if(tempTemperature > 0) {
                binding.currentTemperature.text = "+$tempTemperature" + getTemperatureMark()
            } else if(tempTemperature < 0) {
                binding.currentTemperature.text = "$tempTemperature" + getTemperatureMark()
            } else {
                binding.currentTemperature.text = "0" + getTemperatureMark()
            }

            binding.currentDate.text = dateFormat.format(date.time)
            binding.BIGweatherIcon.background =
                bigWeatherStateImage[currentWeather.weather[0].main]?.let {
                getDrawable(
                    it
                )
            }

            binding.currentWeatherInformationBackground.setOnClickListener {
                val dateList = binding.currentDate.text.split("\n")[1]
                    .split("/")
                val weatherList = presenter.getListByDay(dateList[0], dateList[1], dateList[2])

                if(weatherList != null) {
                    val frag = FragmentWeatherInfo(weatherList, currentWeather, this)
                    frag.show(supportFragmentManager, "Something")
                } else Log.e("Error", "MainActivity: cannot find weather list")
            }

        }

    }

    override fun onClick(v: View?) {
        val fields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val intent = application?.let {
            Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setTypesFilter(listOf(PlaceTypes.CITIES))
                .build(it)
        }
        if (intent != null) {
            startActivityForResult(intent, 300)
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    city = place
                    Log.i("OK", "Places: ${place.name}")

                    changeCity()

                    binding.backToUserGeolocationButton.isClickable = true
                    binding.backToUserGeolocationButton.visibility = View.VISIBLE
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    Log.w("Error:", "Places: Something went wrong")
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    Log.w("Error:", "Places: Cancel")
                }
            }
        }
    }

    private fun changeCity() {
        binding.edittextLocation.hint = city?.address

        val lat = city?.latLng?.latitude
        val lon = city?.latLng?.longitude

        if (lon != null && lat != null) {
            getFor = false
            getCur = false

            binding.refreshLayout.isRefreshing = true

            presenter.updateWeather(lat, lon, unitsOfMeasurement, langCode)
        } else {
            Log.w("Warning", "Places: latitude or longitude is null")
        }

    }

    override fun UpdateCurrentWeather(code: Int) {
        if(binding.refreshLayout.isRefreshing) {
            binding.refreshLayout.isRefreshing = false
        }

        if (code == 200) {
            date = Calendar.getInstance()
            getCur = true

            changeMainCard()
        } else {
            startConnectionDialog(2, code)
        }
    }

    override fun UpdateForecastWeather(code: Int) {
        if(binding.refreshLayout.isRefreshing) {
            binding.refreshLayout.isRefreshing = false
        }

        if (code == 200) {
            date = Calendar.getInstance()
            getFor = true

            val sceneRoot = binding.forTransition as ViewGroup
            val autoTransition = AutoTransition()
            autoTransition.duration = 50
            autoTransition.interpolator = LinearInterpolator()
            TransitionManager.beginDelayedTransition(sceneRoot, autoTransition)
            
            binding.placeForFutureDays.removeAllViews()
            fillScrollView()
        } else {
            startConnectionDialog(3, code)
        }
    }

    override fun UpdateGeolocation(NEWgeolocation: ipGeolocation?, code: Int) {
        if(binding.refreshLayout.isRefreshing) {
            binding.refreshLayout.isRefreshing = false
        }

        if (NEWgeolocation != null) {
            binding.city.text = NEWgeolocation.city
            binding.edittextLocation.hint = NEWgeolocation.city + ", " + NEWgeolocation.country

            getFor = false
            getCur = false

            presenter.updateWeather(NEWgeolocation.lat, NEWgeolocation.lon, unitsOfMeasurement, langCode)
        } else {
            startConnectionDialog(1, code)
        }
    }

    private var getCur : Boolean = false
    private var getFor : Boolean = false
    private fun startConnectionDialog(serviceCode : Int, code: Int) {
        when (serviceCode) {
            1 -> {
                if(!connectionDialogOpen) {
                    connectionDialogOpen = true
                    val connectionDialog = FragmentDialog()
                    connectionDialog.show(
                        supportFragmentManager, "No connection dialog",
                        code, this
                    )
                }
            }
            2 -> {
                if (getFor) {
                    if(!connectionDialogOpen) {
                        connectionDialogOpen = true
                        val connectionDialog = FragmentDialog()
                        connectionDialog.show(
                            supportFragmentManager, "No connection dialog",
                            code, this
                        )
                    }
                } else {
                    getCur = true
                }
            }
            3 -> {
                if (getCur) {
                    if(!connectionDialogOpen) {
                        connectionDialogOpen = true
                        val connectionDialog = FragmentDialog()
                        connectionDialog.show(
                            supportFragmentManager, "No connection dialog",
                            code, this
                        )
                    }
                } else {
                    getFor = true
                }
            }
        }
    }

    override fun DialogResults(code: Int) {
        if(code % 10 == 2) {
            var tempCode = code / 10
            settingsDialogOpen = false
            if(tempCode == 0) return

            var tempAtmoPressure = ""
            when(tempCode % 10) {
                1 -> {
                    tempAtmoPressure = "mmHg"
                }
                2 -> {
                    tempAtmoPressure = "hPa"
                }
            }
            tempCode /= 10
            if(tempAtmoPressure.isNotEmpty() && atmoPressureUnits[0] != tempAtmoPressure[0]) {
                atmoPressureUnits = tempAtmoPressure
            }

            var tempLanguage = ""
            when(tempCode % 10){
                1 -> {
                    tempLanguage = "English"
                }
                2 -> {
                    tempLanguage = "Russian"
                }
            }
            tempCode /= 10
            if(tempLanguage.isNotEmpty() && language[0] != tempLanguage[0]) {
                language = tempLanguage
                if(language[0] == 'E') {
                    langCode = "en"
                } else {
                    langCode = "ru"
                }
                setAppLocale()
            }

            var tempUnits = ""
            when(tempCode % 10){
                1 -> {
                    tempUnits = "standard"
                }
                2 -> {
                    tempUnits = "metric"
                }
                3 -> {
                    tempUnits = "imperial"
                }
            }
            if(tempUnits.isNotEmpty() && unitsOfMeasurement[0] != tempUnits[0]) {
                unitsOfMeasurement = tempUnits

                binding.refreshLayout.isRefreshing = true

                refresh()
            }


            return
        }

        if(code % 10 == 1) {
            getFor = false
            getCur = false

            connectionDialogOpen = false

            if (city != null) {
                val lat = city?.latLng?.latitude
                val lon = city?.latLng?.longitude

                if (lat != null && lon != null) {
                    getCur = false
                    getFor = false

                    binding.refreshLayout.isRefreshing = true

                    presenter.updateWeather(
                        lat, lon, unitsOfMeasurement, langCode
                    )
                } else {
                    Log.w("Warning", "Places: latitude or longitude is null")
                }
            } else {
                binding.refreshLayout.isRefreshing = true

                presenter.updateGeolocation(langCode)
            }
        }
    }

    fun getSpeedMark() : String {
        when(unitsOfMeasurement[0]) {
            's' -> {
                return resources.getString(R.string.speed_standard)
            }
            'm' -> {
                return resources.getString(R.string.speed_metrics)
            }
            'i' -> {
                return resources.getString(R.string.speed_imperial)
            }
        }
        return ""
    }

    fun getDistanceMark() : String {
        when(unitsOfMeasurement[0]) {
            's' -> {
                return resources.getString(R.string.distance_standard)
            }
            'm' -> {
                return resources.getString(R.string.distance_metrics)
            }
            'i' -> {
                return resources.getString(R.string.distance_imperial)
            }
        }
        return ""
    }

    fun getTemperatureMark() : String {
        when(unitsOfMeasurement[0]) {
            's' -> {
               return resources.getString(R.string.temperature_standard)
            }
            'm' -> {
                return resources.getString(R.string.temperature_metrics)
            }
            'i' -> {
                return resources.getString(R.string.temperature_imperial)
            }
        }
        return ""
    }

    fun getAtmoPressureMark() : String {
        when(atmoPressureUnits[0]) {
            'm' -> {
                return resources.getString(R.string.mmHg)
            }
            'h' -> {
                return resources.getString(R.string.hPa)
            }
        }
        return ""
    }

    fun getUnitsOfMeasurement() : String {
        return unitsOfMeasurement
    }

    fun getAtmoPressureUnits() : String {
        return atmoPressureUnits
    }

    private val bigWeatherStateImage = mapOf(
        Pair("Clear", R.drawable.ic_big_clear_day),
        Pair("Clouds", R.drawable.ic_big_cloudy),
        Pair("Mist", R.drawable.ic_big_cloudy),
        Pair("Smoke", R.drawable.ic_big_cloudy),
        Pair("Haze", R.drawable.ic_big_cloudy),
        Pair("Dust", R.drawable.ic_big_cloudy),
        Pair("Fog", R.drawable.ic_big_cloudy),
        Pair("Sand", R.drawable.ic_big_cloudy),
        Pair("Ash", R.drawable.ic_big_cloudy),
        Pair("Squall", R.drawable.ic_big_cloudy),
        Pair("Tornado", R.drawable.ic_big_cloudy),
        Pair("Snow", R.drawable.ic_big_snow),
        Pair("Rain", R.drawable.ic_big_rain),
        Pair("Drizzle", R.drawable.ic_big_rain),
        Pair("Thunderstorm", R.drawable.ic_big_thunderstorm)
    )

    val smallWeatherStateImage = mapOf(
        Pair("Clear", R.drawable.ic_small_clear_day),
        Pair("Clouds", R.drawable.ic_small_cloudy),
        Pair("Mist", R.drawable.ic_small_cloudy),
        Pair("Smoke", R.drawable.ic_small_cloudy),
        Pair("Haze", R.drawable.ic_small_cloudy),
        Pair("Dust", R.drawable.ic_small_cloudy),
        Pair("Fog", R.drawable.ic_small_cloudy),
        Pair("Sand", R.drawable.ic_small_cloudy),
        Pair("Ash", R.drawable.ic_small_cloudy),
        Pair("Squall", R.drawable.ic_small_cloudy),
        Pair("Tornado", R.drawable.ic_small_cloudy),
        Pair("Snow", R.drawable.ic_small_snow),
        Pair("Rain", R.drawable.ic_small_rain),
        Pair("Drizzle", R.drawable.ic_small_rain),
        Pair("Thunderstorm", R.drawable.ic_small_thunderstorm)
    )

    val textColor = mapOf(
        Pair("Clear", R.color.text_clear_day),
        Pair("Clouds", R.color.text_clouds),
        Pair("Mist", R.color.text_clouds),
        Pair("Smoke", R.color.text_clouds),
        Pair("Haze", R.color.text_clouds),
        Pair("Dust", R.color.text_clouds),
        Pair("Fog", R.color.text_clouds),
        Pair("Sand", R.color.text_clouds),
        Pair("Ash", R.color.text_clouds),
        Pair("Squall", R.color.text_clouds),
        Pair("Tornado", R.color.text_clouds),
        Pair("Snow", R.color.text_snow),
        Pair("Rain", R.color.text_rain),
        Pair("Drizzle", R.color.text_rain),
        Pair("Thunderstorm", R.color.text_thunderstorm)
    )

    val cardColor = mapOf(
        Pair("Clear", R.color.clear_day),
        Pair("Clouds", R.color.clouds),
        Pair("Mist", R.color.clouds),
        Pair("Smoke", R.color.clouds),
        Pair("Haze", R.color.clouds),
        Pair("Dust", R.color.clouds),
        Pair("Fog", R.color.clouds),
        Pair("Sand", R.color.clouds),
        Pair("Ash", R.color.clouds),
        Pair("Squall", R.color.clouds),
        Pair("Tornado", R.color.clouds),
        Pair("Snow", R.color.snow),
        Pair("Rain", R.color.rain),
        Pair("Drizzle", R.color.rain),
        Pair("Thunderstorm", R.color.thunderstorm)
    )

    private val whatMoreImportant = mapOf(
        Pair("Clear", 1),
        Pair("Clouds", 2),
        Pair("Mist", 2),
        Pair("Smoke", 2),
        Pair("Haze", 2),
        Pair("Dust", 2),
        Pair("Fog", 2),
        Pair("Sand", 2),
        Pair("Ash", 2),
        Pair("Squall", 2),
        Pair("Tornado", 2),
        Pair("Snow", 3),
        Pair("Rain", 3),
        Pair("Drizzle", 3),
        Pair("Thunderstorm", 4)
    )

    override fun onPause() {
        super.onPause()

        saveInfo.edit().putString("unitsOfMeasurement", unitsOfMeasurement).apply()
        saveInfo.edit().putString("language", language).apply()
        saveInfo.edit().putString("atmoPressureUnits", atmoPressureUnits).apply()
    }

    override fun onResume() {
        super.onResume()

        val oldConnectionFragment = supportFragmentManager.findFragmentByTag("No connection dialog")
        if(oldConnectionFragment != null) {
            supportFragmentManager.beginTransaction().remove(oldConnectionFragment).commit()
        }
    }

}
