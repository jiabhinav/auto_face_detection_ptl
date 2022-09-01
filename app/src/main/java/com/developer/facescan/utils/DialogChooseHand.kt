package com.developer.facescan.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.annotation.RequiresApi
import com.developer.facescan.R
import com.developer.facescan.databinding.CustomDialogBinding
import com.developer.facescan.interfaces.OnClickDialog

class DialogChooseHand(onClickDialog: OnClickDialog, context: Context) : Dialog(context) {
            lateinit var binding:CustomDialogBinding
            var onClickDialogView: OnClickDialog = onClickDialog
        init {
            setCancelable(false)
        }
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window!!.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.white)))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            binding = CustomDialogBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.rlRightHand.setOnClickListener {
                onClickDialogView.onClickDialog(false)
                dismiss()
            }
            binding.rlLeftHand.setOnClickListener {
                onClickDialogView.onClickDialog(true)
                dismiss()
            }
        }
}