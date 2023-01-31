package com.example.weather_app.view

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.weather_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.ceil

class FragmentWeatherInfo: BottomSheetDialogFragment() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ds : DisplayMetrics = resources.displayMetrics
        val layoutHeight = 100 * ds.density
        val layoutWidth = 64 * ds.density

        val layer : View

        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.place_for_somthing)
        layer = layoutInflater.inflate(R.layout.small_weather_card, constraintLayout, false)

        val params : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ceil(layoutWidth).toInt(),
            ceil(layoutHeight).toInt())

        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        layer.layoutParams = params

        Log.i("Fuck", "addview")
        if(constraintLayout == null) Log.i("fuck", "null")
        constraintLayout?.addView(layer)
    }

}