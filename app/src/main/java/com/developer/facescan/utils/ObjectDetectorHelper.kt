/*
package com.developer.facescan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import org.pytorch.*
import org.pytorch.torchvision.TensorImageUtils


import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

 class ObjectDetectorHelper(
  val context: Context,
  val objectDetectorListener: DetectorListener?) {

    lateinit var modelName: Module
    init {
        setupObjectDetector()
    }

    fun setupObjectDetector() {
        // loading serialized torchscript module from packaged into app android asset model.pt,
        // app/src/model/assets/model.pt
         modelName  = LiteModuleLoader.load(assetFilePath(context, "entryv2.ptl"))

    }


    fun detect(image: Bitmap, imageRotation:Int,image2:Image) {
        Log.d("TAG", "detectbitmap: "+image.toString())
     */
/*   if (objectDetector == null) {
            setupObjectDetector()
        }

        // Inference time is the difference between the system time at the start and finish of the
        // process
        var inferenceTime = SystemClock.uptimeMillis()
        Log.e("TAG :::  ", "In Object Helpwe InferenceTime ${inferenceTime}")
        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/
        //            lite_support#imageprocessor_architecture
        val imageProcessor =
            ImageProcessor.Builder()
                .add(Rot90Op(-imageRotation / 90))
                .build()

        // Preprocess the image and convert it into a TensorImage for detection.
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime*//*


     //   Log.d("TAG", "wdqwqccqc : "+Gson().toJson(results))

        // preparing input tensor


        val bit=image.rotate(imageRotation)

        val face_mean = floatArrayOf(116.0f, 117.0f, 111.0f) //offset to {104.0f, 117.0f, 123.0f}

        val face_std = floatArrayOf(1.0f, 1.0f, 1.0f)

       */
/* val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(image,0,0,224,224,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
            MemoryFormat.CHANNELS_LAST
        )*//*

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(image,0,0,224,224,
            face_mean,
            face_std,
            MemoryFormat.CHANNELS_LAST
        )




        // running the model
        val outputTensor: Tensor = modelName.forward(IValue.from(inputTensor)).toTensor()

        val scores = outputTensor.dataAsFloatArray

        Log.d("TAG", "detectfaces=1: "+ Gson().toJson(scores))

        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in 0 until scores.size) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }


       */
/* if (scores[1]>=1.0E-10) {
            maxScoreIdx = 1
            Log.d("TAG", "detectfaces=1: "+Gson().toJson(scores))
        }
        else
        {
            maxScoreIdx = 0
            Log.d("TAG", "detectfaces=0: "+Gson().toJson(scores))
        }*//*



      */
/*  val floatArray = DoubleArray(scores.size)
        for (i in 0 until scores.size) {
            floatArray[i] = scores.get(i).toDouble()
        }*//*


      // val output=softmax(1.0,floatArray)
      // Log.d("TAG", "detectfaces=2: "+maxScoreIdx+"==="+maxScore)
        //Log.d("TAG", "detectfaces=3: "+Gson().toJson(output))

        val className: String = ImageNetClasses.IMAGENET_CLASSES.get(maxScoreIdx)
        Log.d("TAG", "detectfaces: "+className)
        objectDetectorListener?.onResults(className,bit)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun softmax(input: Double, neuronValues: DoubleArray): Double {
        val total: Double = Arrays.stream(neuronValues).map { a: Double ->
            Math.exp(a)
        }.sum()
        return Math.exp(input) / total
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(results: String,bitmap: Bitmap)
    }

    fun Bitmap.rotate(degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, (width*0.8).toInt(), (height*0.8).toInt(), matrix, true)
    }


    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName!!).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }
}

*/
