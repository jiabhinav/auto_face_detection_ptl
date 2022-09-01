package com.developer.facescan.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.developer.facescan.databinding.CustomDialogBinding
import com.developer.facescan.interfaces.OnClickDialog

class CustomDialogClass(var onClickDialog: OnClickDialog, context: Context) : Dialog(context) {
            lateinit var binding:CustomDialogBinding
            var onClickDialogView: OnClickDialog = onClickDialog
        init {
            setCancelable(false)
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
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