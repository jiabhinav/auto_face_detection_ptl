package com.developer.facescan.utils

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.developer.facescan.R
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

    @RequiresApi(Build.VERSION_CODES.M)
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

}