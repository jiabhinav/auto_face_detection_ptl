package com.developer.objectproof.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import com.developer.objectproof.databinding.DialogImagePreviewBinding
import com.developer.objectproof.interfaces.OnClickImageDialog

class ImageDialog(var bitmap: Bitmap,pos: Int,onClickImageDialog: OnClickImageDialog, context: Context): Dialog(context) {
    lateinit var binding: DialogImagePreviewBinding
    val pos: Int = pos
    var onClickImageDialog: OnClickImageDialog = onClickImageDialog
    init {
        setCancelable(false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgPreview.setImageBitmap(bitmap)
        binding.ivClose.setOnClickListener {
            onClickImageDialog.onClickImageDailog(false,pos)
            dismiss()
        }
        binding.ivDelete.setOnClickListener {
            onClickImageDialog.onClickImageDailog(true,pos)
            dismiss()
        }
    }

}