package com.developer.facescan.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import com.developer.facescan.R

class CustomProgress(context: Context) : Dialog(context,android.R.style.Theme_Dialog) {
    var pBar: ProgressBar? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        window!!.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.white)))
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        //    getWindow().setWindowAnimations(R.style.Dialog_animation);
        setContentView(R.layout.custom_progress)
        pBar = findViewById<View>(R.id.custom_progress) as ProgressBar
        setCancelable(false)
    }

    override fun setOnCancelListener(paramOnCancelListener: DialogInterface.OnCancelListener?) {
        super.setOnCancelListener(paramOnCancelListener)
    }

    override fun setOnDismissListener(paramOnDismissListener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(paramOnDismissListener)
    }

}