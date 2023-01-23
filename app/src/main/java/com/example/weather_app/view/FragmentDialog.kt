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
import com.example.weather_app.databinding.FragmentDialogBinding
import com.example.weather_app.view.DialogResult
import kotlin.math.floor

class FragmentDialog : DialogFragment() {

    private lateinit var binding: FragmentDialogBinding
    private lateinit var viewForInfo: DialogResult

    private val code300_str : String = "Oups!\\nSomething went wrong..."
    private val code400_str : String = "Oups!\\nSomething really went wrong..."

    @SuppressLint("DialogFragmentCallbacksDetector")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(context, R.style.dialog)

        val view = layoutInflater.inflate(R.layout.fragment_dialog, null)

        alertDialog.setView(view)
        isCancelable = false


        view?.findViewById<Button>(R.id.try_again_button)?.setOnClickListener{
            dismiss()
        }


        return alertDialog.create()
    }

    fun show(manager: FragmentManager, tag: String, requestCode: Int, dialogResult: DialogResult) {
        Log.i("Info", "FragmentDialog: The dialog is displayed")

        if(requestCode == 300) {
            view?.findViewById<TextView>(R.id.tv_error_message)?.text = code300_str
        } else {
            view?.findViewById<TextView>(R.id.tv_error_message)?.text = code400_str
        }

        viewForInfo = dialogResult
        if(!manager.isDestroyed) {
            show(manager, tag)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.i("Info", "FragmentDialog: Dismiss")

        if (::viewForInfo.isInitialized) {
            viewForInfo.DialogResults(1)
        }
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
