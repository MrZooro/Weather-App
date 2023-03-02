package com.example.weather_app.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.weather_app.R
import kotlin.math.floor

open class FragmentDialog : DialogFragment() {

    private lateinit var viewForInfo: DialogResult
    private var requestCode: Int = 300

    private lateinit var code300Str : String
    private lateinit var code400Str : String

    @SuppressLint("DialogFragmentCallbacksDetector")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(context, R.style.dialog)

        val view = layoutInflater.inflate(R.layout.fragment_dialog, null)

        alertDialog.setView(view)
        isCancelable = false


        view?.findViewById<Button>(R.id.try_again_button)?.setOnClickListener{
            if(::viewForInfo.isInitialized) {
                viewForInfo.DialogResults(1)
            }
            dismiss()
        }


        return alertDialog.create()
    }

    fun show(manager: FragmentManager, tag: String, tempRequestCode: Int, dialogResult: DialogResult) {
        Log.i("Info", "FragmentDialog: The dialog is displayed")
        requestCode = tempRequestCode

        viewForInfo = dialogResult
        if (!manager.isDestroyed) {
            show(manager, tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        code300Str = activity?.getString(R.string.code300Str).toString()
        code400Str = activity?.getString(R.string.code400Str).toString()
    }

    override fun onStart() {
        super.onStart()

        if (requestCode == 300) {
            dialog?.findViewById<TextView>(R.id.tv_error_message)?.text =
                code300Str
        } else {
            dialog?.findViewById<TextView>(R.id.tv_error_message)?.text =
                code400Str
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.i("Info", "FragmentDialog: Dismiss")
    }

    override fun onResume() {
        super.onResume()
        val ds = context?.resources?.displayMetrics?.density
        if(ds != null) {
            val width = 320 * ds

            val window = dialog?.window
            window?.setGravity(Gravity.BOTTOM)

            val params: ViewGroup.LayoutParams = window?.attributes!!
            params.width = floor(width.toDouble()).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog!!.window!!.attributes = params as WindowManager.LayoutParams
        }
    }
}
