package com.developer.facescan.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
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
import com.developer.facescan.R
import com.developer.facescan.interfaces.FileUploadServicePrivate
import com.developer.facescan.model.ResponseTrain
import com.developer.facescan.network.RetrofitClientPrivate
import com.developer.facescan.utils.Utils.dpToPx
import com.developer.facescan.utils.Utils.showToast
import com.developer.facescan.utils.YuvToRgbConverter
import com.developer.facescan.views.CameraActivity
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CameraViewModel : ViewModel(),ImageAnalysis.Analyzer{

    var TAG="CameraViewmodel"
    val palmList = MutableLiveData<ResponseTrain>()
    val errorMessage = MutableLiveData<String>()
    val setUiText=MutableLiveData<String>()
    val bitmap=MutableLiveData<Bitmap>()

    private var lensFacing = CameraSelector.LENS_FACING_FRONT


    var loading = MutableLiveData<Boolean>()
    private lateinit var owner: LifecycleOwner

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

    private var isAuthentication: Boolean = false
    var username=""


    private var yuvToRgbConverter:YuvToRgbConverter? = null
    private lateinit var rotationMatrix: Matrix

    fun uploadToPrivateServer(
         json: JsonObject
    ) {

        onLoader(true)
        val service =
            RetrofitClientPrivate.getInstance().create(FileUploadServicePrivate::class.java)
        val call = service.face_training(json)
        call.enqueue(object : Callback<ResponseTrain> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ResponseTrain>,
                response: retrofit2.Response<ResponseTrain>
            ) {
                onLoader(false)
                if (response.code() == 200) {
                    palmList.postValue(response.body())
                }
               // imageProxy.close()
                Log.d("TAG", "onResponse: " + response.body())

            }

            override fun onFailure(
                call: Call<ResponseTrain>,
                t: Throwable
            ) {
                imageProxy.close()
                errorMessage.postValue(t.message)
                Log.d("dee2e2ff", "onResponse: " + t.message)
                onLoader(false)
            }
        })
    }




    fun authenticateUser(
        json: JsonObject
    ) {
        onLoader(true)
        val service =
            RetrofitClientPrivate.getInstance().create(FileUploadServicePrivate::class.java)
        val call = service.authentication(json)
        call.enqueue(object : Callback<ResponseTrain> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ResponseTrain>,
                response: retrofit2.Response<ResponseTrain>
            ) {
                onLoader(false)

                if (response.code() == 200) {
                    palmList.postValue(response.body())
                }

            }

            override fun onFailure(
                call: Call<ResponseTrain>,
                t: Throwable
            ) {
                errorMessage.postValue(t.message)
                onLoader(false)
            }
        })
    }

    fun onLoader(load: Boolean) {
        loading.value = load

    }

    //=======================CameraView Started here===================================

    fun openCamera(cameraActivity: CameraActivity,layout: PreviewView, isAuthentication: Boolean = true,username:String) {
        previewView = layout
        context = cameraActivity
        owner=cameraActivity
        this.isAuthentication=isAuthentication
        this.username=username
        yuvToRgbConverter= YuvToRgbConverter(context)
       // objectDetectorHelper = ObjectDetectorHelper(context = context, objectDetectorListener = this@CameraViewModel)
        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setUpCamera()
            } else (showToast(context, context.getString(R.string.not_supported)))
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
            val factory: MeteringPointFactory =
                SurfaceOrientedMeteringPointFactory(viewWidth.toFloat(), viewHeight.toFloat())
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
        imageProxy=image
        if (needImageAnalysis)
        {
            detectFaces(image)
        }

    }
    @SuppressLint("UnsafeOptInUsageError")
    private fun detectObjects(image: ImageProxy) {
        imageProxy = image
        val imageRotation = image.imageInfo.rotationDegrees
        // Copy out RGB bits to the shared bitmap buffer
      /*  image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        // Pass Bitmap and rotation to the object detector helper for processing and detection
            objectDetectorHelper.detect(bitmapBuffer, imageRotation)*/

        image.toBitmap()?.let {
            bitmapBuffer=it
            //objectDetectorHelper.detect(bitmapBuffer, imageRotation,image.image!!)
            //imageProxy.close()
        }



    }


    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        //.enableTracking() //disable when contour is enable https://developers.google.com/ml-kit/vision/face-detection/android
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun detectFaces(imageProxy: ImageProxy) {

        val image = InputImage.fromMediaImage(imageProxy.image as Image, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)

            //.addOnSuccessListener(successListener)
           // .addOnFailureListener(failureListener)
            .addOnCompleteListener{
                val faces=it.result
                if (faces.size==1)
                {
                    loop@ for (face in faces) {
                        val boundingBox = RectF(face.boundingBox)

                        val rotX = face.headEulerAngleX // Head is rotated to the right rotX degrees
                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                        val goodConfidence = true //face.get;
                        val min_distance= dpToPx(context.resources.getDimension(R.dimen.min_distance))
                        val max_distance=dpToPx(context.resources.getDimension(R.dimen.max_distance))
                        val rotation_distance=dpToPx(context.resources.getDimension(R.dimen.rotation_distance))
                        Log.d("distances:","min_distance:"+min_distance+"      "+"max_distance:"+max_distance+"      "+"rot_distance"+rotation_distance)
                        Log.d("distances:","width:"+boundingBox.width().toString()+"      "+"height:"+boundingBox.height().toString()+"      "+"rotX:"+rotX+"      "+"rotY:"+rotY+"      "+"rotZ:"+rotZ)

                      /*  if(
                            boundingBox != null && goodConfidence
                            && boundingBox.height() >= min_distance && boundingBox.height()<=max_distance
                            && boundingBox.width() >= min_distance && boundingBox.width()<=max_distance
                            && rotX>=-rotation_distance && rotX<=rotation_distance
                            && rotY>=-rotation_distance && rotY<=rotation_distance
                            && rotZ>=-rotation_distance && rotZ<=rotation_distance)
                        {*/

                        if(
                            boundingBox != null && goodConfidence
                            && boundingBox.height() >= 400 && boundingBox.height()<=660
                            && boundingBox.width() >= 400 && boundingBox.width()<=660
                            && rotX>=-rotation_distance && rotX<=rotation_distance
                            && rotY>=-rotation_distance && rotY<=rotation_distance
                            && rotZ>=-rotation_distance && rotZ<=rotation_distance)
                        {
                            needImageAnalysis=false
                            val imageRotation = imageProxy.imageInfo.rotationDegrees
                            CoroutineScope(Dispatchers.Main).launch {

                              val bitmapBuffer=imageProxy.image!!.toBitmap()
                                val bit=bitmapBuffer.rotate(imageRotation)

                              /*  objectDetectorHelper.detect(bitmapBuffer, imageRotation, imageProxy.image!!)
                                // setUiText.postValue("Face")
                                */
                                setUiText.postValue("Captured")
                                bitmap.postValue(bit)
                                imageProxy.close()


                            }
                        }
                        else
                        {
                            imageProxy.close()
                            setUiText.postValue("Head should be inside the circle")
                        }
                    }



                }
                else if (faces.size==0)
                {
                    setUiText.postValue("Face not found")
                    Log.d("qsdwefrwdwev", ": "+"Face not found")
                    imageProxy.close()
                }
                else
                {
                    Log.d("qsdwefrwdwev", ": "+"Find Multiple face")
                    setUiText.postValue("Find Multiple face ${faces.size}")
                    imageProxy.close()
                }


            }
    }
    fun Bitmap.rotate(degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, (width*0.8).toInt(), (height*0.8).toInt(), matrix, true)
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
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }



    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun ImageProxy.toBitmap(): Bitmap? {
        val image: Image = image ?: return null
        if (!::bitmapBuffer.isInitialized) {
            rotationMatrix = Matrix()
            rotationMatrix.postRotate(this.imageInfo.rotationDegrees.toFloat())
            bitmapBuffer = Bitmap.createBitmap(
                this.width, this.height, Bitmap.Config.ARGB_8888
            )
        }
        // Pass image to an image analyser
        yuvToRgbConverter?.yuvToRgb(image, bitmapBuffer)
        // Create the Bitmap in the correct orientation
        return Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            rotationMatrix,
            false
        )
    }



}