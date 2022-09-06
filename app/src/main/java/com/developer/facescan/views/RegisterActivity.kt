package com.developer.facescan.views

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.biocube.biocube_palm.session.SesssionManager
import com.developer.facescan.R
import com.developer.facescan.databinding.ActivityRegisterBinding
import com.developer.facescan.interfaces.OnClickDialog
import com.developer.facescan.model.ModelUser
import com.developer.facescan.session.DaggerManagerSessComponent
import com.developer.facescan.session.MainActivityModule
import com.developer.facescan.session.ManagerSessComponent
import com.developer.facescan.utils.AppConstants
import com.developer.facescan.utils.DialogChooseHand
import com.developer.facescan.utils.PermissionsDelegate
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RegisterActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener, OnClickDialog {
    private lateinit var binding: ActivityRegisterBinding
    private var genderStr:String=""
    private val permissionsDelegate= PermissionsDelegate(this)
    private var hasPermission: Boolean = false

    @Inject
    lateinit var sess: SesssionManager

    var cal = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setSpinner()
    }

    fun setSpinner(){

        val spinnerAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, resources.getStringArray(R.array.gender))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGender.adapter = spinnerAdapter
        binding.spGender.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {

                genderStr = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun initViews() {
        val component: ManagerSessComponent = DaggerManagerSessComponent
            .builder()
            .mainActivityModule(MainActivityModule(this))
            .build()
        component.inject(this)


        val dialog=  DatePickerDialog(this@RegisterActivity,
            this,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.setMaxDate(cal.timeInMillis)

        binding.btnSubmit.setOnClickListener {
            if(checkValidations()) {
                    callIntent()
                  //  DialogChooseHand(this,this).show()
            }
        }
        binding.etDob.setOnClickListener {
            dialog.show()
        }

        binding.btnFace.setOnClickListener{
            if (sess.checkLogin())
            {
                val intent = Intent(this, CameraActivity::class.java)
                    .putExtra(AppConstants.ISREGISTRATIONMODE,true)
                     startActivity(intent)
            }
            else
            {
                Toast.makeText(this, "You are not registered, You have to register first", Toast.LENGTH_LONG).show()
            }
        }
        }

    override fun onDateSet(p0: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthOfYear)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView()
    }

private fun updateDateInView() {
    val myFormat = "MM/dd/yyyy" // mention the format you need
    val sdf = SimpleDateFormat(myFormat, Locale.US)
    binding.etDob.setText(sdf.format(cal.getTime()).toString())
}

    private fun reset(){
        binding.etFName.setText("")
        binding.spGender.setSelection(0)
        binding.etMName.setText("")
        binding.etLName.setText("")
        binding.etMobile.setText("")
        binding.etAddress.setText("")
        binding.etDob.setText("")
    }

    private fun  checkValidations():Boolean{
        if (TextUtils.isEmpty(binding.etFName.text)){
            showToast(getString(R.string.first_name_empty))
            return false
        }
//        else if (TextUtils.isEmpty(binding.etMName.text)){
//            showToast(getString(R.string.middle_name_empty))
//            return false
//        }
        else if (TextUtils.isEmpty(binding.etLName.text)){
            showToast(getString(R.string.last_name_empty))
            return false
        }
        else if (binding.spGender.selectedItem.toString()==getString(R.string.select_gender)){
            showToast(getString(R.string.select_gender_first))
            return false
        }
        else if (TextUtils.isEmpty(binding.etMobile.text)){
            showToast(getString(R.string.mobile_empty))
            return false
        }

        else if (TextUtils.isEmpty(binding.etDob.text)){
            showToast(getString(R.string.birth_date_empty))
            return false
        }
//        else if (TextUtils.isEmpty(binding.etAddress.text)){
//            showToast(getString(R.string.address_empty))
//            return false
//        }
//        if (!binding.tncCheckbox.isChecked){
//            showToast(getString(R.string.check_validation))
//            return false
//        }
        return true
    }
    fun showToast(msg: String){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }

    override fun onClickDialog(isLefHand: Boolean) {
        var handside=""
        if(isLefHand){
            handside=resources.getString(R.string.left)
        }else{
            handside=resources.getString(R.string.right)
        }
        val random=(100000..999999).random()

        val model= ModelUser().apply {
            this.id=random
            this.firstname=binding.etFName.text.toString().replace(" ", "_")
            this.lastname=binding.etLName.text.toString().replace(" ", "_")
            this.mobile=binding.etMobile.text.toString()
            this.dob=binding.etDob.text.toString()
            this.gender=genderStr
            this.embedding=""

        }
        sess.sessionLogin(model)

        val intent = Intent(this, CameraActivity::class.java)
            .putExtra(AppConstants.ISREGISTRATIONMODE,true)
            startActivity(intent)
              reset()
    }
    //            .putExtra(AppConstants.MNAME,binding.etMName.text.toString().replace(" ", "_"))
    //            .putExtra(AppConstants.AGE,binding.etAddress.text.toString())
    override fun onResume() {
        super.onResume()
        if (permissionsDelegate.hasPermissions() && !hasPermission) {
            hasPermission = true
        } else {
            permissionsDelegate.requestPermissions()
        }
    }

    fun callIntent()
    {
        val random=(100000..999999).random()
        val model= ModelUser().apply {
            this.id=random
            this.firstname=binding.etFName.text.toString().replace(" ", "_")
            this.lastname=binding.etLName.text.toString().replace(" ", "_")
            this.mobile=binding.etMobile.text.toString()
            this.dob=binding.etDob.text.toString()
            this.gender=genderStr
            this.embedding=""
        }
        sess.sessionLogin(model)

        val intent = Intent(this, CameraActivity::class.java)
            .putExtra(AppConstants.ISREGISTRATIONMODE,true)
        startActivity(intent)
        reset()
    }

}