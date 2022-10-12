package com.developer.objectproof.utils

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import com.developer.objectproof.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    fun bitmapToFile(context: Context, bitmap: Bitmap, extension: String, filename: String): File? { // File name like "image.png"
        //create a file to write bitmap data

        val fileName = filename + extension
        val directory: File = context.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        var file: File? = File(directory, fileName)
        return try {

//            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            if (file != null) {
                file.createNewFile()
            }


            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()
            Log.e("TAG Bitmap ::: ", bitmapdata.toString())
            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }
    fun bitmapToString(context: Context, bitmap: Bitmap, extension: String, filename: String): String? { // File name like "image.png"
        //create a file to write bitmap data

        val fileName = filename + extension
        val directory: File = context.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        var file: File? = File(directory, fileName)
    var str=""
        return try {

//            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            if (file != null) {
                file.createNewFile()
            }

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()


            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()

            return str



        } catch (e: Exception) {
            e.printStackTrace()
            str // it will return null
        }
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }


    fun showLoadingDialog(context: Context): Dialog {
        val progressDialog = Dialog(context, R.style.MyDialogTheme)
        if (progressDialog.window != null) {
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        return progressDialog
    }

    fun dpToPx(dp: Float): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun getBase64image(bitmap: Bitmap):String
    {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return imageString
    }

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun imgToBitmap(image: Image): Bitmap? {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }




}