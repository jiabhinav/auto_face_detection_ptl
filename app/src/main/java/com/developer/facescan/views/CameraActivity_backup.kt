package com.developer.facescan.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.developer.facescan.adapters.FingerprintsAdapter
import com.developer.facescan.databinding.ActivityCameraBinding
import com.developer.facescan.interfaces.*
import com.developer.facescan.model.Response
import com.developer.facescan.network.RetrofitClientPrivate
import com.developer.facescan.utils.*
import com.developer.facescan.viewmodel.CameraViewModel
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.tensorflow.lite.task.vision.detector.Detection
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.developer.facescan.R

class CameraActivity_backup : AppCompatActivity(), ImageAnalysis.Analyzer,
    ObjectDetectorHelper.DetectorListener, OnImageClick, OnClickImageDialog,OnClickScanedDialog {
    private var inflater: LayoutInflater?= null
    private lateinit var hand_sd: RequestBody
    private lateinit var features: RequestBody
    private lateinit var userid_sd: RequestBody
    private lateinit var mobile_sd: RequestBody
    private lateinit var dob_sd: RequestBody
    private lateinit var index_sd: RequestBody
    private var isLeftDone: Boolean = true
    private var isRightDone: Boolean = false
    private var rotation: Int = 0
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: androidx.camera.core.Camera? = null
    private var age: String = ""
    private var username: String = ""
    private var mobile: String = ""
    private var gender: String = ""
    private var index: Int = 1
    private var isAuthentication: Boolean = false
    private var handside: String = ""
    private var randomID: String = ""
    private val threshold: Float = 0.98f
    private var progressPercent = 40
    private lateinit var binding: ActivityCameraBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var needImageAnalysis = true
    private var alFingerPrints: ArrayList<Bitmap> = ArrayList()
    private lateinit var adapter: FingerprintsAdapter
    private var scorecount: Int = 0
    private lateinit var imageProxy: ImageProxy
   lateinit var customProgress:Dialog
    private var progressDialog: AlertDialog? = null
    private  var dialogBuilder: AlertDialog.Builder? = null

    lateinit var viewModel: CameraViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        getIntentData()
        initView()
        observAPIData()


    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initView() {

        customProgress = Utils.showLoadingDialog(this)
       inflater = this.layoutInflater
        dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)

        /* Recyclerview Adapter initialization */
        adapter = FingerprintsAdapter(this, alFingerPrints, this)
        binding.rvImages.adapter = adapter
        /* ObjectDetectorHelper initialization */
        objectDetectorHelper = ObjectDetectorHelper(context = this, objectDetectorListener = this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.viewFinder.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setUpCamera()
            } else (Utils.showToast(this, getString(R.string.not_supported)))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getIntentData() {
        val isRegistration = intent.getBooleanExtra(AppConstants.ISREGISTRATIONMODE, true)
        if (isRegistration) {
            age = intent.getStringExtra(AppConstants.DOB).toString()
            gender = intent.getStringExtra(AppConstants.GENDER).toString()
            username = intent.getStringExtra(AppConstants.FNAME).toString() + intent.getStringExtra(
                AppConstants.LNAME
            )
            mobile = intent.getStringExtra(AppConstants.GENDER).toString()
        }
        handside = intent.getStringExtra(AppConstants.HANDSIDE).toString()
        randomID = intent.getStringExtra(AppConstants.RANDOM).toString()
        if (handside.contains(getString(R.string.right))) {
            isLeftDone = false
            isRightDone = true
           // binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_right))
        }
        if (!isRegistration) {
            isAuthentication = true
        }
        GlobalScope.launch {
            features= RequestBody.create(MediaType.parse("text/plain"), "4422")
            hand_sd = RequestBody.create(MediaType.parse("text/plain"), handside)
            dob_sd = RequestBody.create(MediaType.parse("text/plain"), age)
            mobile_sd = RequestBody.create(MediaType.parse("text/plain"), mobile)
            userid_sd = RequestBody.create(MediaType.parse("text/plain"), username + randomID)
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()
                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        rotation = binding.viewFinder.display.rotation
        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetResolution(Size(1080, 1920))
                .setTargetRotation(rotation)
                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetResolution(Size(1080, 1920))
                .setTargetRotation(rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
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
                    binding.viewFinder.width,
                    binding.viewFinder.height
                ),
                binding.viewFinder.display.rotation
            ).setScaleType(ViewPort.FILL_CENTER).build()

            val useCaseGroupBuilder: UseCaseGroup.Builder = UseCaseGroup.Builder().setViewPort(
                viewPort
            )
            useCaseGroupBuilder.addUseCase(preview!!)
            useCaseGroupBuilder.addUseCase(imageAnalyzer!!)

            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroupBuilder.build())

            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
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
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//            autoFocus()
            toggleFlash(true)

        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun setCameraControl(camera: Camera) {
        val cameraControl = camera.cameraControl
        cameraControl.setExposureCompensationIndex(-1)
        cameraControl.setZoomRatio(2.0f)
    }

    private fun autoFocus() {
        camera?.let {
            setCameraAutoFocus(
                it,
                binding.viewFinder!!.width,
                binding.viewFinder.height
            )
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
        imageProxy = image
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            // the analyzer has started running
            bitmapBuffer = Bitmap.createBitmap(
                image.width,
                image.height,
                Bitmap.Config.ARGB_8888
            )
        }
        if (needImageAnalysis) {
            detectObjects(imageProxy)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
      //  objectDetectorHelper.detect(bitmapBuffer,imageRotation,image.image)
    }

    override fun onError(error: String) {
        runOnUiThread {
            Utils.showToast(this, error)
        }
    }

    override fun onResults(results: String, bitmap: Bitmap) {
        TODO("Not yet implemented")
    }



    @RequiresApi(Build.VERSION_CODES.N)
     fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        if (results != null) {
            for (result in results) {
                Log.e("TAG ::: confidence ", "${result.categories[0].score}")

                if (result.categories[0].score >= threshold) {
                    scorecount += 1
                    runOnUiThread {
                        setUiText(getString(R.string.finger_detected), true)
                    }
                    Log.e("TAG::: sc" ,"${scorecount}")
                    if (scorecount > 1) {
                        autoFocus()
                    }
                    Thread.sleep(1000)
                    val matrix = Matrix().apply {
                        postRotate(rotation.toFloat())
                    }




                    if (!isAuthentication) {
                        if (alFingerPrints.size < 1 && scorecount > 1) {
                            runOnUiThread { setUiText(getString(R.string.capturing), true) }
                            val uprightImage = Bitmap.createBitmap(
                                bitmapBuffer,
                                0,
                                0,
                                bitmapBuffer.width,
                                bitmapBuffer.height,
                                matrix,
                                true
                            )
                            val randomno = (100000..999999).random().toString()
                            val file = Utils.bitmapToFile(
                                this,
                                uprightImage,
                                ".png",
                                username + "_${randomno}"
                            )
                            alFingerPrints.add(uprightImage)
                            runOnUiThread { adapter.notifyDataSetChanged() }
                            val fingerBody = RequestBody.create(MediaType.parse("image/*"), file)
                            val imageForServer = MultipartBody.Part.createFormData(
                                "img",
                                file!!.getName(),
                                fingerBody
                            )
                            needImageAnalysis = false
                            progressPercent += 20
                            toggleFlash(false)
                            if (Utils.isOnline(this)) {
                               // uploadToPrivateServer(imageForServer)
                                runOnUiThread()
                                {
                                    viewModel.uploadToPrivateServer(userid_sd,hand_sd,imageForServer)
                                }



                            } else {
                                Utils.showToast(this, getString(R.string.network_error))
                            }
                        }
                    } else {
                        if (alFingerPrints.size < 1 && scorecount > 2) {
                            val uprightImage = Bitmap.createBitmap(
                                bitmapBuffer,
                                0,
                                0,
                                bitmapBuffer.width,
                                bitmapBuffer.height,
                                matrix,
                                true
                            )
                            val randomno = (100000..999999).random().toString()
                            val file = Utils.bitmapToFile(
                                this,
                                uprightImage,
                                ".png",
                                username + "_${randomno}"
                            )
                            alFingerPrints.add(uprightImage)
                            needImageAnalysis = false
                            runOnUiThread { adapter.notifyDataSetChanged() }
                            val fingerBody = RequestBody.create(MediaType.parse("image/*"), file)

                            val imageForServer = MultipartBody.Part.createFormData(
                                "img",
                                file!!.getName(),
                                fingerBody
                            )

                            if (Utils.isOnline(this)) {
                                    toggleFlash(false)
                                  //  authenticateUser(imageForServer)
                                    runOnUiThread {
                                        viewModel.authenticateUser(features,hand_sd,imageForServer)
                                    }


                            } else {
                                Utils.showToast(this, getString(R.string.network_error))
                            }
                        }
                    }

                } else {
                    imageProxy.close()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        toggleFlash(false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClickImage(position: Int) {
        ImageDialog(alFingerPrints.get(position), position, this, this).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClickImageDailog(isDelete: Boolean, position: Int) {
        if (isDelete) {
            scorecount = 0
            alFingerPrints.removeAt(position)
            adapter.notifyDataSetChanged()
            needImageAnalysis = true
            imageProxy.close()
            toggleFlash(true)
            setUiText(getString(R.string.capturing), true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        if (adapter.itemCount <= 1) {
            toggleFlash(true)
        } else {
            toggleFlash(false)
        }
    }

    fun uploadToPrivateServer(imageForServer: MultipartBody.Part) {
        runOnUiThread {
            showProgress(true)
            setUiText(getString(R.string.uploading), true)
        }

        GlobalScope.launch {
            val startTime: Date
            startTime = Calendar.getInstance().getTime()
            Log.e("Server StartTime :: ", startTime.toString())
        }
        val service =
            RetrofitClientPrivate.getInstance().create(FileUploadServicePrivate::class.java)

        var time = RequestBody.create(
            MediaType.parse("text/plain"),
            System.currentTimeMillis().toString()
        );

        val call = service.upload(userid_sd, hand_sd, imageForServer)
        call.enqueue(object : Callback<Response> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<Response>,
                response: retrofit2.Response<Response>
            ) {
                Log.e("TAG ::: ", "Inside Response 506 line")

                 showProgress(false)
                if (response.code() == 200) {
                    GlobalScope.launch {
                        val startTime: Date
                        startTime = Calendar.getInstance().getTime()
                        Log.e("Server EndTime :: ", startTime.toString())
                    }
                    index += 1
                    Log.e("TAG ::: ", "Inside Response 516 line")

                    needImageAnalysis = false
//                    Utils.showToast(this@CameraActivity, progressPercent.toString() + "% Fingerprint Uploaded")
                    if (response.body()!!.output.contains("False")) {
                        showAuthenticationResult(
                            response.body()!!.message,
                            response.body()!!.output
                        )
                    }else{
                        setUiText(getString(R.string.successfully_captured), true)
                        needImageAnalysis = false
                        Log.e("TAG", " Right done ${isRightDone}  Left done ${isLeftDone}")
                        DialogScanFingers(
                            isLeftDone,
                            isRightDone,
                            this@CameraActivity_backup,
                            this@CameraActivity_backup
                        ).show()
                    }
                }
                imageProxy.close()
            }

            override fun onFailure(
                call: Call<com.developer.facescan.model.Response>,
                t: Throwable
            ) {
                showProgress(false)
            }
        })
    }

    private fun showProgress(isShow: Boolean) {
        Log.e("TAG ::: ", "Inside show progress ${isShow}")
        if (isShow)
            customProgress.show()
        if (!isShow)
            customProgress.dismiss()
    }


    fun authenticateUser(imageForServer: MultipartBody.Part) {
        runOnUiThread {
            showProgress(true)
            setUiText(getString(R.string.authenticating), true)
        }

        val service =
            RetrofitClientPrivate.getInstance().create(FileUploadServicePrivate::class.java)
        val call = service.authentication(
            features,
            hand_sd,
            imageForServer
        )
        call.enqueue(object : Callback<Response> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<Response>,
                response: retrofit2.Response<Response>
            ) {
                showProgress(false)
                if (response.code() == 200) {
                    GlobalScope.launch {
                        val endTime: Date
                        endTime = Calendar.getInstance().getTime()
                        Log.e("Server EndTime :: ", endTime.toString())
                    }
                    imageProxy.close()
                    if(response.body()!!.output.equals("False")) {
                        showAuthenticationResult(
                            response.body()!!.message,
                            response.body()!!.output
                        )
                    }else{
                        showAuthenticationResult(
                            response.body()!!.message,
                            response.body()!!.output)
                    }

                }
            }

            override fun onFailure(
                call: Call<Response>,
                t: Throwable
            ) {
                showProgress(false)
            }
        })
    }

    fun setUiText(message: String, shouldVisible: Boolean) {
        if (shouldVisible) {
            binding.cvStatus.visibility = View.VISIBLE
        } else {
            binding.cvStatus.visibility = View.GONE
        }
        binding.txtStatus.setText(message)
    }

    override fun onClickScannedDialog(isLeft: Boolean, isRight: Boolean) {
        Log.e("TAG::: ", "isLeftDone ${isLeftDone}  isRightDone ${isRightDone}")
        if (isLeftDone && isRightDone){
            finish()
        }
        else if (isLeftDone) {
            features= RequestBody.create(MediaType.parse("text/plain"), "4422")
            hand_sd = RequestBody.create(MediaType.parse("text/plain"), getString(R.string.right))
           // binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_right))
             isRightDone=true

        } else {
            features= RequestBody.create(MediaType.parse("text/plain"), "4422")
            hand_sd = RequestBody.create(MediaType.parse("text/plain"), getString(R.string.left))
           // binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_left))
             isLeftDone=true
        }
        alFingerPrints.clear()
        scorecount=0
        autoFocus()
        adapter.notifyDataSetChanged()
        binding.txtStatus.setText("")
        needImageAnalysis = true
        imageProxy.close()

    }

    private fun showAuthenticationResult(message: String, output: String) {

        val dialogView = inflater!!.inflate(R.layout.dialog_success, null)
        dialogBuilder!!.setView(dialogView)

        val txtDone = dialogView.findViewById(R.id.txt_okay) as TextView
        val title = dialogView.findViewById(R.id.title) as TextView
        val messagetxt = dialogView.findViewById(R.id.message) as TextView
        messagetxt.text = message

        txtDone.setOnClickListener {
            if (output.contains("False")) {
                scorecount=0
                alFingerPrints.clear()
                adapter.notifyDataSetChanged()
                binding.txtStatus.setText(R.string.capturing)
                needImageAnalysis = true
                toggleFlash(true)
                imageProxy.close()
            }else {
                finish()
            }
            progressDialog!!.dismiss()
        }
        progressDialog = dialogBuilder!!.create()
        progressDialog!!.window!!.attributes.windowAnimations = R.style.DialogTheme
        progressDialog!!.setCancelable(false)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()
    }

    fun observAPIData() {

        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        viewModel.palmList.observe(this,  {
            if (!isAuthentication)
            {
                Log.d("wd1wdw1d1", "init: "+ Gson().toJson(it))
                Toast.makeText(this, ""+ Gson().toJson(it), Toast.LENGTH_LONG).show()

                GlobalScope.launch {
                    val startTime: Date
                    startTime = Calendar.getInstance().getTime()
                    Log.e("Server EndTime :: ", startTime.toString())
                }
                index += 1
                Log.e("TAG ::: ", "Inside Response 516 line")

                needImageAnalysis = false
//                    Utils.showToast(this@CameraActivity, progressPercent.toString() + "% Fingerprint Uploaded")
                if (it.output.contains("False")) {
                    showAuthenticationResult(it.message, it.output)
                }else{
                    setUiText(getString(R.string.successfully_captured), true)
                    needImageAnalysis = false
                    Log.e("TAG", " Right done ${isRightDone}  Left done ${isLeftDone}")
                    DialogScanFingers(
                        isLeftDone,
                        isRightDone,
                        this@CameraActivity_backup,
                        this@CameraActivity_backup
                    ).show()
                }

                imageProxy.close()
            }
            else
            {
                GlobalScope.launch {
                    val endTime: Date
                    endTime = Calendar.getInstance().getTime()
                    Log.e("Server EndTime :: ", endTime.toString())
                }
                imageProxy.close()
                if(it.output.equals("False")) {
                    showAuthenticationResult(
                        it.message,
                        it.output
                    )
                }else{
                    showAuthenticationResult(
                       it.message,
                        it.output)
                }
            }


        })

        viewModel.errorMessage.observe(this,  {
            Log.d("1d2wd2ddd", "init: "+ Gson().toJson(it))
            Toast.makeText(this, ""+it, Toast.LENGTH_LONG).show()



        })

        viewModel.loading.observe(this,{
            if (it)
            {
                showProgress(true)
            }
            else
            {
                showProgress(false)
            }
        })
    }
}