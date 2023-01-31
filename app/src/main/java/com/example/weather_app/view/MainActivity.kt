package com.example.weather_app.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.weather_app.BuildConfig
import com.example.weather_app.R
import com.example.weather_app.databinding.ActivityMainBinding
import com.example.weather_app.model.current_weather.CurrentWeather
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

    private val dateFormat : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.white)

        Places.initialize(applicationContext, BuildConfig.Places_API_KEY, Locale.ENGLISH)
        Places.createClient(this)

        binding.edittextLocation.isFocusable = false
        binding.edittextLocation.setOnClickListener(this)

        presenter = Presenter(this)
        presenter.updateGeolocation()

        binding.backToUserGeolocationButton.setOnClickListener{
            binding.backToUserGeolocationButton.isClickable = false
            binding.backToUserGeolocationButton.visibility = View.GONE
            city = null
            presenter.updateGeolocation()
        }

        binding.reloadWeatherButton.setOnClickListener{

            /*
            val frag = FragmentWeatherInfo()
            frag.show(supportFragmentManager, "Something")
             */

            if(city != null) {
                val lat = city?.latLng?.latitude
                val lon = city?.latLng?.longitude

                if(lat != null && lon != null) {
                    getCur = false
                    getFor = false
                    presenter.updateWeather(lat, lon, BuildConfig.OpenWeatherMap_API_KEY)
                } else {
                    Log.w("Warning", "Places: latitude or longitude is null")
                }
            } else {
                presenter.updateGeolocation()
            }


        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun fillScrollView() {
        var layer : View
        val inflater : LayoutInflater = layoutInflater

        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 100 * ds.density
        val layoutWidth = 64 * ds.density
        val defaultMargin = 72 * ds.density
        var layoutMargin = 18 * ds.density

        val tempCalendar = date

        var forecastIdElem = 0

        val sceneRoot = binding.forTransition as ViewGroup
        val autoTransition = AutoTransition()
        autoTransition.duration = 50
        autoTransition.interpolator = LinearInterpolator()

        val forecastWeatherByDay = presenter.getForecastListWeatherByDay()

        for(element in forecastWeatherByDay) {

            var minTemp = element[0].main.temp_min
            var maxTemp = element[0].main.temp_max
            var whatIcon = ""
            var iconImportance = 0

            for(j in 1 until element.size) {
                if(element[j].main.temp_min < minTemp) minTemp = element[j].main.temp_min

                if(element[j].main.temp_max > maxTemp) maxTemp = element[j].main.temp_max

                val tempIconImportance = whatMoreImportant[element[j].weather[0].main]
                if(tempIconImportance != null) {
                    if(tempIconImportance > iconImportance) {
                        whatIcon = element[j].weather[0].main
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
            params.marginStart = layoutMargin.toInt()
            layer.layoutParams = params

            tempCalendar.add(Calendar.DATE, 1)

            layer.findViewById<TextView>(R.id.card_date).text = dateFormat.format(tempCalendar.time)

            val minTempInt = (minTemp - 273.15).roundToInt()
            val maxTempInt = (maxTemp - 273.15).roundToInt()

            var tempStr: String = if (minTempInt < 0) {
                "$minTempInt .. "
            } else if (minTempInt > 0){
                "+$minTempInt .. "
            } else {
                "0 .. "
            }

            tempStr += if(maxTempInt < 0) {
                "$maxTempInt"
            } else if (maxTempInt > 0) {
                "+$maxTempInt"
            } else {
                "0"
            }

            layer.findViewById<TextView>(R.id.card_temperature).text = tempStr

            layer.findViewById<View>(R.id.card_weather_icon).background =
                SMALLweatherStateImage[whatIcon]?.let { getDrawable(it) }

            TransitionManager.beginDelayedTransition(sceneRoot, autoTransition)
            binding.placeForFutureDays.addView(layer)
            layoutMargin += defaultMargin
            forecastIdElem += 8
        }



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

            val tempTemperature = ((currentWeather.main.temp - 273.15).roundToInt())
            if(tempTemperature > 0) {
                binding.currentTemperature.text = "+$tempTemperature°C"
            } else if(tempTemperature < 0) {
                binding.currentTemperature.text = "$tempTemperature°C"
            } else {
                binding.currentTemperature.text = "0"
            }

            binding.currentDate.text = dateFormat.format(date.time)
            binding.BIGweatherIcon.background =
                BIGweatherStateImage[currentWeather.weather[0].main]?.let {
                getDrawable(
                    it
                )
            }

        }

    }

    override fun onClick(v: View?) {
        val fields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val intent = application?.let {
            Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setTypesFilter(Arrays.asList(PlaceTypes.CITIES))
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
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                city = place
                Log.i("OK", "Places: ${place.name}")

                changeCity()

                binding.backToUserGeolocationButton.isClickable = true
                binding.backToUserGeolocationButton.visibility = View.VISIBLE
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Log.w("Error:", "Places: Something went wrong")
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                Log.w("Error:", "Places: Cancel")
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

            presenter.updateWeather(lat, lon, BuildConfig.OpenWeatherMap_API_KEY)
        } else {
            Log.w("Warning", "Places: latitude or longitude is null")
        }

    }

    override fun UpdateCurrentWeather(code: Int) {
        if (code == 200) {
            date = Calendar.getInstance()
            getCur = true

            changeMainCard()
        } else {
            startDialog(2, code)
        }
    }

    override fun UpdateForecastWeather(code: Int) {
        if (code == 200) {
            date = Calendar.getInstance()
            getFor = true

            binding.placeForFutureDays.removeAllViews()
            fillScrollView()
        } else {
            startDialog(3, code)
        }
    }

    override fun UpdateGeolocation(NEWgeolocation: ipGeolocation?, code: Int) {
        if (NEWgeolocation != null) {
            binding.city.text = NEWgeolocation.city
            binding.edittextLocation.hint = NEWgeolocation.city + ", " + NEWgeolocation.country

            getFor = false
            getCur = false

            presenter.updateWeather(NEWgeolocation.lat, NEWgeolocation.lon, BuildConfig.OpenWeatherMap_API_KEY)
        } else {
            startDialog(1, code)
        }
    }

    private var getCur : Boolean = false
    private var getFor : Boolean = false
    private  val connectionDialog = FragmentDialog()
    private fun startDialog(serviceCode : Int, code: Int) {

        println(serviceCode)
        when (serviceCode) {
            1 -> {
                connectionDialog.show(
                    supportFragmentManager, "No connection dialog",
                    code, this
                )
            }
            2 -> {
                if (getFor) {
                    connectionDialog.show(
                        supportFragmentManager, "No connection dialog",
                        code, this
                    )
                } else {
                    getCur = true
                }
            }
            3 -> {
                if (getCur) {
                    connectionDialog.show(
                        supportFragmentManager, "No connection dialog",
                        code, this
                    )
                } else {
                    getFor = true
                }
            }
        }
    }

    override fun DialogResults(code: Int) {
        getFor = false
        getCur = false

        if(city != null) {
            val lat = city?.latLng?.latitude
            val lon = city?.latLng?.longitude

            if(lat != null && lon != null) {
                getCur = false
                getFor = false
                presenter.updateWeather(lat, lon, BuildConfig.OpenWeatherMap_API_KEY)
            } else {
                Log.w("Warning", "Places: latitude or longitude is null")
            }
        } else {
            presenter.updateGeolocation()
        }
    }

    val BIGweatherStateImage = mapOf(
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

    val SMALLweatherStateImage = mapOf(
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

    val whatMoreImportant = mapOf(
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


    override fun onStop() {
        super.onStop()
        if(connectionDialog.isAdded) {
            connectionDialog.dismissAllowingStateLoss()
        }
    }

}
