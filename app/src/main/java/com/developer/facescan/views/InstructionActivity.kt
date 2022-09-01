package com.developer.facescan.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.developer.facescan.R
import com.developer.facescan.adapters.InstructionAdapter
import com.developer.facescan.databinding.ActivityInstructionBinding
import com.developer.facescan.interfaces.OnClickDialog
import com.developer.facescan.utils.AppConstants
import com.developer.facescan.utils.DialogChooseHand
import com.developer.facescan.utils.MarginItemDecoration
import com.developer.facescan.utils.PermissionsDelegate
import com.developer.facescan.viewmodel.InstructionViewModel
import kotlin.collections.ArrayList

class InstructionActivity : AppCompatActivity(), OnClickDialog {
    private lateinit var binding: ActivityInstructionBinding
    private lateinit var adapter: InstructionAdapter
    private var alInstrucions = ArrayList<String>()
    private var alInstrucions2= ArrayList<String>()
    lateinit var viewModel:InstructionViewModel
    private val permissionsDelegate= PermissionsDelegate(this)
    private var hasPermission: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instruction)
        binding.rvInstructions.addItemDecoration(MarginItemDecoration(42))


        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnAuthenticate.setOnClickListener {

            if (permissionsDelegate.hasPermissions() ) {
                DialogChooseHand(this,this).show()
            } else {
                permissionsDelegate.requestPermissions()
            }

        }
        adapter = InstructionAdapter(alInstrucions, this)
        binding.rvInstructions.adapter = adapter

        viewModel = ViewModelProvider(this).get(InstructionViewModel::class.java)

        val strInstructions = resources.getStringArray(R.array.instructions)
        alInstrucions.addAll(strInstructions)
        observeData()
        viewModel.add(strInstructions)
    }

    fun observeData()
    {
        viewModel.alInstructionModel.observe(this,{
            alInstrucions.clear()
            alInstrucions.addAll(it)
            Log.d("dwdqefwf", "observeData: "+it)
            adapter.notifyDataSetChanged()
        })
    }



    override fun onClickDialog(isLefHand: Boolean) {
        var handside=""
        if(isLefHand){
            handside=resources.getString(R.string.left)
        }else{
            handside=resources.getString(R.string.right)
        }
        val random=(100000..999999).random().toString()
        val intent = Intent(this, CameraActivity::class.java)
            .putExtra(AppConstants.ISREGISTRATIONMODE,false)
            .putExtra(AppConstants.HANDSIDE,handside)
            .putExtra(AppConstants.RANDOM,random)
        startActivity(intent)

    }
}