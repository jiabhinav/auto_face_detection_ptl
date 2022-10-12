package com.developer.objectproof.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developer.objectproof.R
import com.developer.objectproof.utils.OverlayView
import com.developer.objectproof.utils.Utils
import com.developer.objectproof.utils.Utils.imgToBitmap
import com.developer.objectproof.utils.Utils.showToast
import com.developer.objectproof.utils.Utils.toBitmap
import com.developer.objectproof.views.CameraActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CameraViewModel : ViewModel(),ImageAnalysis.Analyzer{

    var TAG="CameraViewmodel"
    val setUiText=MutableLiveData<String>()
    val bitmap=MutableLiveData<Bitmap>()
    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var owner: LifecycleOwner
    private var overlayView: OverlayView?=null
   lateinit var localModel:LocalModel

    @Inject
    lateinit var retrofit: Retrofit
    init {
         localModel = LocalModel.Builder()
            .setAssetFilePath("model.tflite")
            // or .setAbsoluteFilePath(absolute file path to model file)
            // or .setUri(URI to model file)
            .build()
    }




    //===============CameraPreview Started=================
//    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    @SuppressLint("StaticFieldLeak")
    private lateinit var previewView: PreviewView
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Activity
    private var rotation: Int = 0
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    lateinit var imageProxy: ImageProxy
    lateinit var bitmapBuffer: Bitmap
    var needImageAnalysis = true

    var isAuthentication: Boolean = false
    var username=""


    private lateinit var rotationMatrix: Matrix



    fun onLoader(load: Boolean) {
        loading.value = load

    }

    //=======================CameraView Started here===================================

    fun openCamera(cameraActivity: CameraActivity, layout: PreviewView, isAuthentication: Boolean = true,  overlayView: OverlayView?=null) {
        previewView = layout
        context = cameraActivity
        owner=cameraActivity
        this.isAuthentication=isAuthentication
        this.username=username
        this.overlayView=overlayView
        // objectDetectorHelper = ObjectDetectorHelper(context = context, objectDetectorListener = this@CameraViewModel)
        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setUpCamera()
            } else {
                showToast(context, "Not Supported")
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()
                // Build and bind the camera use cases
                bindCameraUseCases(context)
            },
            ContextCompat.getMainExecutor(context)
        )


    }

    fun flipCamera()
    {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bindCameraUseCases(context)
        }
        else
        {
            Toast.makeText(context, "Device not supported!!", Toast.LENGTH_SHORT).show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases(context: Activity) {
        rotation = previewView.display.rotation
        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder()
            .setTargetResolution(Size(1080, 1920))
            .setTargetRotation(Surface.ROTATION_0)
            .build()


        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetResolution(Size(1080, 1920))
                .setTargetRotation(Surface.ROTATION_0)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

        imageAnalyzer!!.setAnalyzer(
            cameraExecutor,
            this
        )
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            val viewPort: ViewPort = ViewPort.Builder(
                Rational(
                    previewView.width,
                    previewView.height
                ),
                previewView.display.rotation
            ).setScaleType(ViewPort.FILL_CENTER).build()

            val useCaseGroupBuilder: UseCaseGroup.Builder = UseCaseGroup.Builder().setViewPort(
                viewPort
            )
            useCaseGroupBuilder.addUseCase(preview!!)
            useCaseGroupBuilder.addUseCase(imageAnalyzer!!)

            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(owner, cameraSelector, useCaseGroupBuilder.build())

            val manager =
                context.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
            for (cameraId in manager.cameraIdList) {
                //CameraCharacteristics characteristics
                val mCameraInfo = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this project.
                val facing = mCameraInfo.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                setCameraControl(camera!!)
            }

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(previewView.surfaceProvider)
//            autoFocus()
            // toggleFlash(true)

        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }

    fun toggleFlash(enable: Boolean) {
        if (camera != null) {
            val cameraInfo = camera!!.cameraInfo
            if (camera!!.cameraInfo.hasFlashUnit() && cameraInfo.torchState.value != null) {
                camera!!.cameraControl.enableTorch(enable)
            }
            setCameraControl(camera!!)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun setCameraControl(camera: Camera) {
        val cameraControl = camera.cameraControl
        cameraControl.setExposureCompensationIndex(-1)
        cameraControl.setZoomRatio(1.0f)
    }



    fun autoFocus() {
        camera?.let {
            setCameraAutoFocus(
                it,
                previewView.width,
                previewView.height
            )
        }

    }

    fun setCameraAutoFocus(camera: Camera, viewWidth: Int, viewHeight: Int) {
        try {
            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(viewWidth.toFloat(), viewHeight.toFloat())
            val centerWidth = viewWidth / 2
            val centreHeight = viewHeight / 2
            val autoFocusPoint = factory.createPoint(centerWidth.toFloat(), centreHeight.toFloat())
            val builder = FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
            builder.setAutoCancelDuration(10L, TimeUnit.SECONDS)
            camera.cameraControl.startFocusAndMetering(builder.build())
        } catch (var8: java.lang.Exception) {
        }
    }



    override fun analyze(image: ImageProxy) {
        Log.d("face", "in analyze")

        imageProxy=image
        if (needImageAnalysis)
        {
            Log.d("face", "in analyze detectface")
            detectFaces(image)
        }

    }



   /* private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        //.enableTracking() //disable when contour is enable https://developers.google.com/ml-kit/vision/face-detection/android
        .build()
    private val detector = FaceDetection.getClient(options)*/

 private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .enableClassification() /// Optional
        .build()

//    var options: CustomObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
//        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE) //Detecting object from live camera feed
//        .enableClassification() // Optional
//        .setClassificationConfidenceThreshold(0.5f)
//        .setMaxPerObjectLabelCount(3)
//        .build()

    val detector = ObjectDetection.getClient(options)


    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun detectFaces(imageProxy: ImageProxy) {
        val imageRotation = imageProxy.imageInfo.rotationDegrees
        val mediaImage = imageProxy.image
        val bit=imgToBitmap(mediaImage!!)
        //Log.d(TAG, "detectFaces: "+bit)

        val image = InputImage.fromMediaImage(mediaImage, imageRotation)
        detector.process(image).addOnSuccessListener({
                if (it.size==1)
                {
                    loop@ for (face in it) {
                        val boundingBox = RectF(face.boundingBox)

                        val top = boundingBox.top * 1f
                        val left = boundingBox.left * 1f

                        /*  val t=100.0f
                          val b=1000.0f
                          val l=5.0f
                          val r=1000.0f*/

                        Log.d(TAG, "detectFaces: "+"Top:"+top+", left:"+ left)

                        if(top>=540 && top<=750 && left>=20 && left<=100)
                        {

                           // needImageAnalysis=false

                          CoroutineScope(Dispatchers.IO).launch {
                              // val bitmapBuffer=imageProxy.image!!.toBitmap()
                              //  val bit=bitmapBuffer.rotate(imageRotation)
                            val bit2=bit?.rotate(imageRotation)
                            bitmap.postValue(bit2)
                            setUiText.postValue("Captured")
                            imageProxy.close()

                            }


                        }
                        else
                        {
                            Log.d("Object", "in top right else part")

                            imageProxy.close()
                            setUiText.postValue(context.getString(R.string.face_inside))
                        }
                    }



                }
                else if (it.size==0)
                {
                    setUiText.postValue("Object not found")
                    imageProxy.close()
                }
                else
                {
                    setUiText.postValue("Find Multiple Object ${it.size}")
                    imageProxy.close()
                }
            })

    }
    fun Bitmap.rotate(degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }















}