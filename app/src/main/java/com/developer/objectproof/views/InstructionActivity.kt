package com.developer.objectproof.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.biocube.biocube_palm.session.SesssionManager
import com.developer.objectproof.R
import com.developer.objectproof.adapters.InstructionAdapter
import com.developer.objectproof.databinding.ActivityInstructionBinding
import com.developer.objectproof.interfaces.OnClickDialog
import com.developer.objectproof.session.DaggerManagerSessComponent
import com.developer.objectproof.session.MainActivityModule
import com.developer.objectproof.session.ManagerSessComponent
import com.developer.objectproof.utils.AppConstants
import com.developer.objectproof.utils.MarginItemDecoration
import com.developer.objectproof.utils.PermissionsDelegate
import com.developer.objectproof.viewmodel.InstructionViewModel
import javax.inject.Inject
import kotlin.collections.ArrayList

class InstructionActivity : AppCompatActivity(), OnClickDialog {
    private lateinit var binding: ActivityInstructionBinding
    private lateinit var adapter: InstructionAdapter
    private var alInstrucions = ArrayList<String>()
    private var alInstrucions2= ArrayList<String>()
    lateinit var viewModel:InstructionViewModel
    private val permissionsDelegate= PermissionsDelegate(this)
    private var hasPermission: Boolean = false
    @Inject
    lateinit var sess: SesssionManager


    var permission=false
    var isLogin=false


    override fun onStart() {
        super.onStart()
        val component: ManagerSessComponent = DaggerManagerSessComponent
            .builder()
            .mainActivityModule(MainActivityModule(this))
            .build()
        component.inject(this)
        permission=permissionsDelegate.hasPermissions()
        isLogin=sess.checkLogin()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instruction)

        binding.rvInstructions.addItemDecoration(MarginItemDecoration(42))


        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnAuthenticate.setOnClickListener {

            if (permissionsDelegate.hasPermissions()) {
               // DialogChooseHand(this,this).show()
                if (sess.checkLogin())
                {
                    val intent = Intent(this, CameraActivity::class.java)
                        .putExtra(AppConstants.ISREGISTRATIONMODE,false)
                        startActivity(intent)
                }
                else
                {
                    Toast.makeText(this, "You are not registered, You have to register first", Toast.LENGTH_LONG).show()
                }

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