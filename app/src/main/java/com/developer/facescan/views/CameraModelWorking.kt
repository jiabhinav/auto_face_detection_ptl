//package com.developer.fingerbioscan.views
//
//import android.annotation.SuppressLint
//import android.database.DatabaseUtils
//import android.graphics.Bitmap
//import android.graphics.Camera
//import android.graphics.RectF
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.content.ContextCompat
//import androidx.databinding.DataBindingUtil
//import com.developer.fingerbioscan.R
//import com.developer.fingerbioscan.databinding.ActivityCameraBinding
//import com.developer.fingerbioscan.utils.ObjectDetectorHelper
//import com.developer.fingerbioscan.utils.OverlayView
//import org.tensorflow.lite.task.vision.detector.Detection
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class CameraActivity : AppCompatActivity(), ImageAnalysis.Analyzer,ObjectDetectorHelper.DetectorListener  {
//    private var random:String=""
//    //    private var m_cameraExecutorService: ExecutorService? = null
//    private var m_preview: Preview? = null
//    private var imageAnalyzer: ImageAnalysis? = null
//    private var camera: androidx.camera.core.Camera? = null
//    private var m_cameraSelector: CameraSelector? = null
//    private var m_cameraProvider: ProcessCameraProvider? = null
//    private val m_lensFacing = CameraSelector.LENS_FACING_BACK
//    private var age:String=""
//    private var username:String=""
//    private var gender:String=""
//    private var randomID:String=""
//    private var progressPercent =0
//    private lateinit var binding: ActivityCameraBinding
//    private lateinit var objectDetectorHelper: ObjectDetectorHelper
//    private lateinit var bitmapBuffer: Bitmap
//    private lateinit var cameraExecutor: ExecutorService
//    private var cameraProvider: ProcessCameraProvider? = null
//    private var preview: Preview? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding= DataBindingUtil.setContentView(this,R.layout.activity_camera)
//        objectDetectorHelper = ObjectDetectorHelper(
//            context = this,
//            objectDetectorListener = this)
//        cameraExecutor = Executors.newSingleThreadExecutor()
//        binding.viewFinder.post {
//            setUpCamera()
//        }
//
//    }
//
//    private fun setUpCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener(
//            {
//                // CameraProvider
//                cameraProvider = cameraProviderFuture.get()
//
//                // Build and bind the camera use cases
//                bindCameraUseCases()
//            },
//            ContextCompat.getMainExecutor(this)
//        )
//    }
//
//    // Declare and bind preview, capture and analysis use cases
//    @SuppressLint("UnsafeOptInUsageError")
//    private fun bindCameraUseCases() {
//
//        // CameraProvider
//        val cameraProvider =
//            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
//
//        // CameraSelector - makes assumption that we're only using the back camera
//        val cameraSelector =
//            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
//
//        // Preview. Only using the 4:3 ratio because this is the closest to our models
//        preview =
//            Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.viewFinder.display.rotation)
//                .build()
//
//        // ImageAnalysis. Using RGBA 8888 to match how our models work
//        imageAnalyzer =
//            ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(binding.viewFinder.display.rotation)
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                .build()
//
//        imageAnalyzer!!.setAnalyzer(
//            cameraExecutor!!,
//            this
//        )
//        // Must unbind the use-cases before rebinding them
//        cameraProvider.unbindAll()
//
//        try {
//            // A variable number of use-cases can be passed here -
//            // camera provides access to CameraControl & CameraInfo
//            camera= cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
//
//            // Attach the viewfinder's surface provider to preview use case
//            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//        } catch (exc: Exception) {
//            Log.e("TAG", "Use case binding failed", exc)
//        }
//    }
//    override fun analyze(image: ImageProxy) {
//        if (!::bitmapBuffer.isInitialized) {
//            // The image rotation and RGB image buffer are initialized only once
//            // the analyzer has started running
//            bitmapBuffer = Bitmap.createBitmap(
//                image.width,
//                image.height,
//                Bitmap.Config.ARGB_8888
//            )
//        }
//        Log.e("TAG", "Inside Image Analysis")
//
//        detectObjects(image)
//    }
//    private fun detectObjects(image: ImageProxy) {
//        // Copy out RGB bits to the shared bitmap buffer
//        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
//
//        val imageRotation = image.imageInfo.rotationDegrees
//        // Pass Bitmap and rotation to the object detector helper for processing and detection
//        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
//    }
//    override fun onError(error: String) {
//        runOnUiThread {
//            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onResults(
//        results: MutableList<Detection>?,
//        inferenceTime: Long,
//        imageHeight: Int,
//        imageWidth: Int
//    ) {
//        runOnUiThread {
//
////            binding.txtStatus.text =
////                String.format("%d ms", inferenceTime)
//
//            if (results != null) {
//                for (result in results) {
//                    val boundingBox = result.boundingBox
//
//
//
//
//
//                    // Create text to display alongside detected objects
//                    val drawableText =
//                        result.categories[0].label + " " +
//                                String.format("%.2f", result.categories[0].score)
//                    if (result.categories[0].score>0.98f) {
//                        binding.txtStatus.text = drawableText
//                        Log.e("TAG", "Confidence is 99 click picture")
//                    }
//
//                    // Draw text for detected object
//                }
//            }
//
//            // Pass necessary information to OverlayView for drawing on the canvas
////            binding.overlay.setResults(
////                results ?: LinkedList<Detection>(),
////                imageHeight,
////                imageWidth
////            )
////
////            // Force a redraw
////            binding.overlay.invalidate()
//        }
//    }
//}

//===========================================================


//    fun uploadToServer(imageForServer: MultipartBody.Part) {
//        var startTime: Date
//        startTime = Calendar.getInstance().getTime()
//        Log.e("Start Time ", startTime.toString())
//        val service = RetrofitClient.getInstance().create(FileUploadService::class.java)
//
//        var ageN = RequestBody.create(MediaType.parse("text/plain"), age);
//        var name = RequestBody.create(MediaType.parse("text/plain"), username);
//        var genderr = RequestBody.create(MediaType.parse("text/plain"), gender);
//        var id = RequestBody.create(MediaType.parse("text/plain"), randomID);
//        var time = RequestBody.create(
//            MediaType.parse("text/plain"),
//            System.currentTimeMillis().toString()
//        );
//
//        val call = service.upload(
//
//            ageN,
//            genderr,
//            id,
//            name,
//            time,
//            imageForServer
//        )
//        call.enqueue(object : Callback<ResponseBody> {
//            @RequiresApi(Build.VERSION_CODES.N)
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.code() == 200) {
//                    adapter.notifyDataSetChanged()
//                    var endTime: Date
//                    endTime = Calendar.getInstance().getTime()
//                    Log.e("End Time  ", endTime.toString())
//
//                    Toast.makeText(
//                        this@CameraActivity,
//                        progressPercent.toString() + "% Fingerprint Uploaded",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    binding.progress.visibility = View.GONE
//
//                    if (progressPercent == 100) {
//                        finish()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                binding.progress.visibility = View.GONE
//                Log.e("TAG", "failed")
//            }
//        })
//    }