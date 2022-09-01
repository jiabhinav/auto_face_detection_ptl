package com.developer.facescan.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InstructionViewModel: ViewModel() {
    var alInstructionModel = MutableLiveData<Array<String>>()
    var alInstructionModelNew = arrayListOf<String>()

    fun add(list: Array<String>){
        alInstructionModel.postValue(list)
    }

}