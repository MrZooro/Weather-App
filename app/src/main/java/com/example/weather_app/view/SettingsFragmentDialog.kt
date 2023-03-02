package com.example.weather_app.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.weather_app.R
import com.example.weather_app.databinding.SettingsDialogFragmentBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class SettingsFragmentDialog(private val viewForInfo: DialogResult,
                             private val units: String,
                             private val lang: String,
                             private val atmoPressureUnits: String) : DialogFragment() {

    private var code = 2

    @SuppressLint("DialogFragmentCallbacksDetector")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(context, R.style.settings_dialog)

        val view = layoutInflater.inflate(R.layout.settings_dialog_fragment, null)

        view.findViewById<AutoCompleteTextView>(R.id.units_of_measurement_dropdown_menu).setText(units, false)
        view.findViewById<AutoCompleteTextView>(R.id.languages_dropdown_menu).setText(lang, false)
        view.findViewById<AutoCompleteTextView>(R.id.atmoPressure_dropdown_menu).setText(atmoPressureUnits, false)

        alertDialog.setView(view)
        isCancelable = true

        view.findViewById<Button>(R.id.close_button_settings).setOnClickListener{
            viewForInfo.DialogResults(2)
            dismiss()
        }

        view.findViewById<Button>(R.id.save_button_settings).setOnClickListener{
            code = 0
            val unitsStr = view.findViewById<AutoCompleteTextView>(R.id.units_of_measurement_dropdown_menu).text
            if(unitsStr.isNotEmpty()) {
                when (unitsStr[0]) {
                    's' -> {
                        code = 1
                    }
                    'm' -> {
                        code = 2
                    }
                    'i' -> {
                        code = 3
                    }
                }
            }
            code *= 10

            val langStr = view.findViewById<AutoCompleteTextView>(R.id.languages_dropdown_menu).text
            if(langStr.isNotEmpty()) {
                when (langStr[0]) {
                    'E' -> {
                        code += 1
                    }
                    'Р' -> {
                        code += 2
                    }
                }
            }
            code *= 10

            val atmoPressureStr = view.findViewById<AutoCompleteTextView>(R.id.atmoPressure_dropdown_menu).text
            if(atmoPressureStr.isNotEmpty()) {
                when(atmoPressureStr[0]) {
                    'm' -> {
                       code += 1
                    }
                    'h' -> {
                        code += 2
                    }
                }
            }
            code = code * 10 + 2

            dismiss()
        }

        return alertDialog.create()
    }

    private lateinit var binding: SettingsDialogFragmentBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsDialogFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val window = dialog?.window
        window?.setGravity(Gravity.CENTER)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewForInfo.DialogResults(code)
    }
}