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
import com.developer.facescan.R
import com.developer.facescan.databinding.ActivityRegisterBinding
import com.developer.facescan.interfaces.OnClickDialog
import com.developer.facescan.utils.AppConstants
import com.developer.facescan.utils.DialogChooseHand
import com.developer.facescan.utils.PermissionsDelegate
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener, OnClickDialog {
    private lateinit var binding: ActivityRegisterBinding
    private var genderStr:String=""
    private val permissionsDelegate= PermissionsDelegate(this)
    private var hasPermission: Boolean = false
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
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
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
        val dialog=  DatePickerDialog(this@RegisterActivity,
            this,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.setMaxDate(cal.timeInMillis)

        binding.btnSubmit.setOnClickListener {
            if(checkValidations()) {
                    DialogChooseHand(this,this).show()
            }
        }
        binding.etDob.setOnClickListener {
            dialog.show()
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
        var random=(100000..999999).random().toString()
        val intent = Intent(this, CameraActivity::class.java)
            .putExtra(AppConstants.FNAME,binding.etFName.text.toString().replace(" ", "_"))
            .putExtra(AppConstants.LNAME,binding.etLName.text.toString().replace(" ", "_"))
            .putExtra(AppConstants.MOBILE,binding.etMobile.text.toString())

            .putExtra(AppConstants.DOB,binding.etDob.text.toString())
            .putExtra(AppConstants.GENDER,genderStr)
            .putExtra(AppConstants.ISREGISTRATIONMODE,true)
            .putExtra(AppConstants.HANDSIDE,handside)
            .putExtra(AppConstants.RANDOM,random)
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

}