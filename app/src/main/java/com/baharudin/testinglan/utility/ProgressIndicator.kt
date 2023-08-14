package com.baharudin.testinglan.utility

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.baharudin.testinglan.R

class ProgressIndicator internal constructor(context: Context?) : Activity() {
    private val mProgressDialog: AlertDialog?
    private val mProgressDialogMessage: TextView

    init {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_progress, null)
        mProgressDialogMessage = view.findViewById<TextView>(R.id.progressMessage)
        builder.setView(view)
        mProgressDialog = builder.create()
        mProgressDialog.setCancelable(false)
    }

    fun beginProgress(msg: String?) {
        runOnUiThread {
            mProgressDialogMessage.text = msg
            mProgressDialog!!.show()
        }
    }

    // Change progress
    protected fun changeProgress(msg: String?) {
        runOnUiThread { mProgressDialogMessage.text = msg }
    }

    // End progress
    fun endProgress() {
        runOnUiThread { mProgressDialog?.dismiss() }
    }
}
