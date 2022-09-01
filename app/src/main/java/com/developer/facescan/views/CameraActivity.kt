package com.developer.facescan.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.developer.facescan.R
import com.developer.facescan.adapters.FingerprintsAdapter
import com.developer.facescan.databinding.ActivityCameraBinding
import com.developer.facescan.interfaces.OnClickImageDialog
import com.developer.facescan.interfaces.OnClickScanedDialog
import com.developer.facescan.interfaces.OnImageClick
import com.developer.facescan.utils.AppConstants
import com.developer.facescan.utils.DialogScanFingers
import com.developer.facescan.utils.ImageDialog
import com.developer.facescan.utils.Utils.isOnline
import com.developer.facescan.utils.Utils.showLoadingDialog
import com.developer.facescan.utils.Utils.showToast
import com.developer.facescan.viewmodel.CameraViewModel
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import java.util.*


class CameraActivity : AppCompatActivity(),
    OnImageClick, OnClickImageDialog,OnClickScanedDialog, SensorEventListener {
    private var inflater: LayoutInflater?= null
    private lateinit var hand_sd: RequestBody
    private lateinit var features: RequestBody
    private lateinit var userid_sd: RequestBody
    private lateinit var mobile_sd: RequestBody
    private lateinit var dob_sd: RequestBody
    private var isLeftDone: Boolean = true
    private var isRightDone: Boolean = false
    private var rotation: Int = 0

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

    private var alFingerPrints: ArrayList<Bitmap> = ArrayList()
    private lateinit var adapter: FingerprintsAdapter
    private var scorecount: Int = 0

   lateinit var customProgress:Dialog
    private var progressDialog: AlertDialog? = null
    private  var dialogBuilder: AlertDialog.Builder? = null

    lateinit var viewModel: CameraViewModel

    private lateinit var sensorManager: SensorManager
    var gravity: FloatArray? =null
    var geoMagnetic:FloatArray?=null
    var accelerometer:Sensor?=null
    var gyroscope:Sensor?=null
    var linearAcceleration:Sensor?=null
    var rotationVector:Sensor?=null
    val sensorHeight=1.4
    var magnetSensor:Sensor?=null
    var accSensor:Sensor?=null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        getIntentData()
        initView()
        observAPIData()
        Sensor()


    }



    @RequiresApi(Build.VERSION_CODES.N)
    private fun initView() {

        customProgress = showLoadingDialog(this)
       inflater = this.layoutInflater
        dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)

        /* Recyclerview Adapter initialization */
        adapter = FingerprintsAdapter(this, alFingerPrints, this)
        binding.rvImages.adapter = adapter
        viewModel.openCamera(this,binding.viewFinder,isAuthentication,username)
        binding.flip.setOnClickListener {
            viewModel.flipCamera()
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
          //  binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_right))
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
    override fun onPause() {
        super.onPause()
        viewModel.toggleFlash(false)
        sensorManager.unregisterListener(this)
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
            viewModel.needImageAnalysis = true
            viewModel.imageProxy.close()
            viewModel.toggleFlash(true)
            setUiText(getString(R.string.capturing), true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        if (adapter.itemCount <= 1) {
            viewModel.toggleFlash(true)
        } else {
            viewModel.toggleFlash(false)
        }

        sensorManager.registerListener(this, accSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetSensor,
            SensorManager.SENSOR_DELAY_NORMAL)

        /*if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }*/
    }


    private fun showProgress(isShow: Boolean) {
        Log.e("TAG ::: ", "Inside show progress ${isShow}")
        if (isShow)
            customProgress.show()
        if (!isShow)
            customProgress.dismiss()
    }




    fun setUiText(message: String, shouldVisible: Boolean=true) {
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
          //  binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_right))
             isRightDone=true

        } else {
            features= RequestBody.create(MediaType.parse("text/plain"), "4422")
            hand_sd = RequestBody.create(MediaType.parse("text/plain"), getString(R.string.left))
          //  binding.ivTransparentView.setImageDrawable(getDrawable(R.drawable.ic_left))
             isLeftDone=true
        }
        alFingerPrints.clear()
        scorecount=0
        viewModel.autoFocus()
        adapter.notifyDataSetChanged()
        binding.txtStatus.setText("")
        viewModel.needImageAnalysis = true
        viewModel.imageProxy.close()

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
                viewModel.needImageAnalysis = true
                viewModel.toggleFlash(true)
                viewModel.imageProxy.close()
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

                viewModel.needImageAnalysis = false
//                    Utils.showToast(this@CameraActivity, progressPercent.toString() + "% Fingerprint Uploaded")
                if (it.output.contains("False")) {
                    showAuthenticationResult(it.message, it.output)
                }else{
                    setUiText(getString(R.string.successfully_captured), true)
                    viewModel.needImageAnalysis = false
                    Log.e("TAG", " Right done ${isRightDone}  Left done ${isLeftDone}")
                    DialogScanFingers(
                        isLeftDone,
                        isRightDone,
                        this@CameraActivity,
                        this@CameraActivity
                    ).show()
                }

                viewModel.imageProxy.close()
            }
            else
            {
                GlobalScope.launch {
                    val endTime: Date
                    endTime = Calendar.getInstance().getTime()
                    Log.e("Server EndTime :: ", endTime.toString())
                }
                viewModel.imageProxy.close()
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
        viewModel.setUiText.observe(this,{
            setUiText(it)
        })
        viewModel.captureBitmap.observe(this,{
            alFingerPrints=it
            adapter.notifyDataSetChanged()
        })

        viewModel.callAPI.observe(this,{
            val fingerBody = RequestBody.create(MediaType.parse("image/*"), it)
            val imageForServer = MultipartBody.Part.createFormData(
                "img",
                it!!.getName(),
                fingerBody
            )
            viewModel.toggleFlash(false)
            if (isAuthentication)
            {
                if (isOnline(this)) {
                    viewModel.authenticateUser(features,hand_sd,imageForServer)
                } else {
                    showToast(this, getString(R.string.network_error))
                }
            }
            else
            {
                if (isOnline(this))
                {
                    viewModel.uploadToPrivateServer(userid_sd,hand_sd,imageForServer)
                }
                else
                {
                   showToast(this, getString(R.string.network_error))
                }

            }

        })

        viewModel.bitmap.observe(this,{
            alFingerPrints.add(it)
            adapter.notifyDataSetChanged()
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


    fun Sensor()
    {
        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

       /* sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)?.let {
           // this.gravity = it
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            this.gyroscope = it
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
            this.linearAcceleration = it
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let {
            this.rotationVector = it
        }*/

        accSensor = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        magnetSensor = sensorManager
            .getDefaultSensor(TYPE_MAGNETIC_FIELD);

    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.getType() === TYPE_ACCELEROMETER)
             gravity = event.values
        if (event?.sensor?.getType() === TYPE_MAGNETIC_FIELD)
            geoMagnetic = event.values
        if (gravity != null && geoMagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(
                R, I, gravity,
                geoMagnetic
            )
            if (success) {
                /* Orientation has azimuth, pitch and roll */
                val orientation = FloatArray(3)
                //SensorManager.remapCoordinateSystem(R, 1, 3, orientation);
                SensorManager.getOrientation(R, orientation)

                var azimut = 57.29578f * orientation[0]
               var  pitch = 57.29578f * orientation[1]
               var roll = 57.29578f * orientation[2]
                val d: Double = Math.tan(
                    Math.toRadians(
                        Math.abs(pitch)
                            .toDouble()
                    )
                ) * sensorHeight

                val dist_por = Math.abs((1.4f * Math.tan(roll * Math.PI / 180)).toFloat()).toDouble()
               // val dist_lan = Math.abs((1.4f * Math.tan(pitch * Math.PI / 180)).toFloat()).toDouble()
              //  Log.d("TAG", "onSensorChanged: "+"Distance is " +dist_por+"=="+dist_lan)
                val dis = String.format("%.4f", dist_por)
                //binding.distance.text=dis

            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }





}