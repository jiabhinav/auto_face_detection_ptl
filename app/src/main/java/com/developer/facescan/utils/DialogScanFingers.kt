package com.developer.facescan.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import com.developer.facescan.R
import com.developer.facescan.databinding.DialogFingerScanBinding
import com.developer.facescan.interfaces.OnClickScanedDialog

class DialogScanFingers(isLeftDone: Boolean,isRightDone: Boolean, onClickDialog: OnClickScanedDialog, context: Context) : Dialog(context) {
            lateinit var binding:DialogFingerScanBinding
            var onClickDialogView: OnClickScanedDialog = onClickDialog
            var isLeftDone = isLeftDone
            var isRightDone = isRightDone
        init {
            setCancelable(false)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window!!.setBackgroundDrawable(ColorDrawable(context.getColor(R.color.white)))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            binding = DialogFingerScanBinding.inflate(layoutInflater)
            setContentView(binding.root)
            if(isLeftDone && isRightDone){
                binding.tvTitle.setText(context.getString(R.string.successfully_registered))
                binding.ivDoneright.visibility= View.VISIBLE
                binding.ivDoneleft.visibility= View.VISIBLE
                binding.rlLeftHand.isClickable=false
                binding.ivLeftHand.isClickable=false
                binding.rlRightHand.isClickable=false
                binding.ivRightHand.isClickable=false
                binding.txtOkay.visibility=View.VISIBLE
            }
           else if(isLeftDone){
                binding.tvTitle.setText(context.getString(R.string.continue_with_right))
                binding.ivDoneright.visibility= View.GONE
                binding.rlRightHand.isClickable=false
                binding.ivRightHand.isClickable=false
            }else {
                binding.tvTitle.setText(context.getString(R.string.continue_with_left))
                binding.ivDoneleft.visibility= View.GONE
                binding.rlLeftHand.isClickable=false
                binding.ivLeftHand.isClickable=false
            }

            binding.txtOkay.setOnClickListener { onClickDialogView.onClickScannedDialog(isLeftDone,isRightDone) }
            binding.rlRightHand.setOnClickListener {
                onClickDialogView.onClickScannedDialog(isLeftDone,isRightDone)
                dismiss()
            }
            binding.rlLeftHand.setOnClickListener {
                onClickDialogView.onClickScannedDialog(isLeftDone,isRightDone)
                dismiss()
            }
        }
}