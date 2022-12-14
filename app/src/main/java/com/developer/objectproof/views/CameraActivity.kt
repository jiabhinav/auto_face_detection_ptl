package com.developer.objectproof.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
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
import com.biocube.biocube_palm.session.SesssionManager
import com.developer.objectproof.R
import com.developer.objectproof.adapters.FingerprintsAdapter
import com.developer.objectproof.databinding.ActivityCameraBinding
import com.developer.objectproof.interfaces.OnClickImageDialog
import com.developer.objectproof.interfaces.OnClickScanedDialog
import com.developer.objectproof.interfaces.OnImageClick
import com.developer.objectproof.session.DaggerManagerSessComponent
import com.developer.objectproof.session.MainActivityModule
import com.developer.objectproof.session.ManagerSessComponent
import com.developer.objectproof.utils.AppConstants
import com.developer.objectproof.utils.ImageDialog
import com.developer.objectproof.utils.Utils.getBase64image
import com.developer.objectproof.utils.Utils.isOnline
import com.developer.objectproof.utils.Utils.showLoadingDialog
import com.developer.objectproof.utils.Utils.showToast
import com.developer.objectproof.viewmodel.CameraViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


class CameraActivity : AppCompatActivity(),
    OnImageClick, OnClickImageDialog,OnClickScanedDialog {
    private var inflater: LayoutInflater?= null
    private var isLeftDone: Boolean = true
    private var isRightDone: Boolean = false
    private var rotation: Int = 0

    private var index: Int = 1
    private var isAuthentication: Boolean = false
    private  var username:String=""
    private var handside: String = ""

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

    @Inject
    lateinit var sess: SesssionManager


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        getIntentData()
         initView()
         observAPIData()

    }




    @RequiresApi(Build.VERSION_CODES.N)
    private fun initView() {

        username=sess.getUser()!!.firstname+" "+sess.getUser()!!.lastname
        binding.txtUsername.text=username


        customProgress = showLoadingDialog(this)
       inflater = this.layoutInflater
        dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)

        /* Recyclerview Adapter initialization */
        adapter = FingerprintsAdapter(this, alFingerPrints, this)
        binding.rvImages.adapter = adapter
        GlobalScope.launch {
            viewModel.openCamera(this@CameraActivity,binding.viewFinder,isAuthentication,binding.overlay)
        }

        binding.flip.setOnClickListener {
            viewModel.flipCamera()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getIntentData() {

        val component: ManagerSessComponent = DaggerManagerSessComponent
            .builder()
            .mainActivityModule(MainActivityModule(this))
            .build()
        component.inject(this)

        val isRegistration = intent.getBooleanExtra(AppConstants.ISREGISTRATIONMODE, true)
        if (!isRegistration) {
            isAuthentication = true
        }

    }



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        viewModel.toggleFlash(false)

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
      /*  if (adapter.itemCount <= 1) {
            viewModel.toggleFlash(true)
        } else {
            viewModel.toggleFlash(false)
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
            binding.txtStatus.visibility = View.VISIBLE
        } else {
            binding.txtStatus.visibility = View.GONE
        }
        binding.txtStatus.setText(message)
    }

    override fun onClickScannedDialog(isLeft: Boolean, isRight: Boolean) {
        Log.e("TAG::: ", "isLeftDone ${isLeftDone}  isRightDone ${isRightDone}")
        alFingerPrints.clear()
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
                binding.txtStatus.setText(R.string.capturing)
                viewModel.needImageAnalysis = true
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


        viewModel.setUiText.observe(this,{
            setUiText(it)
        })


        viewModel.bitmap.observe(this,{
             alFingerPrints.clear()
             alFingerPrints.add(it)
            adapter.notifyDataSetChanged()
           // callAPI(it)
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
                setUiText("")
                showProgress(false)
            }
        })


    }

    fun callAPI(bitmap: Bitmap)
    {
            if (!isAuthentication)
            {
                if (isOnline(this)) {
                  //  viewModel.authenticateUser(features,hand_sd,imageForServer)
                    var gsonObject = JsonObject()
                        val jsonObj = JSONObject()
                        jsonObj.put("image", getBase64image(bitmap))
                        jsonObj.put("liveliness", true)
                        val jsonParser = JsonParser()
                        gsonObject = jsonParser.parse(jsonObj.toString()) as JsonObject
                        Log.e("MY gson.JSON:  ", "AS PARAMETER  $gsonObject")
                   // viewModel.uploadToPrivateServer(gsonObject)
                } else {
                    showToast(this, getString(R.string.network_error))
                }
            }
            else {
                if (isOnline(this)) {
                    var gsonObject = JsonObject()
                    val jsonObj = JSONObject()
                    jsonObj.put("image", getBase64image(bitmap))
                    jsonObj.put("encoding", sess.getUser()!!.embedding)
                    val jsonParser = JsonParser()
                    gsonObject = jsonParser.parse(jsonObj.toString()) as JsonObject
                    Log.e("MY gson.JSON:  ", "AS PARAMETER  $gsonObject")
                        //  viewModel.authenticateUser(gsonObject)
                } else {
                    showToast(this, getString(R.string.network_error))
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()

    }





}