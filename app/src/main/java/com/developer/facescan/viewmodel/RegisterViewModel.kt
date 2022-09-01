package com.developer.facescan.viewmodel
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*



class RegisterViewModel() : ViewModel() {

  /*  Basic details Edittext field*/
    var first_name:String?=""
    var last_name:String?=""
    var mobile_number:String?=""


    /*  Error Basic details Edittext field*/
   var errorfirstname = ObservableField<String>()
    var errorlastname = ObservableField<String>()
    var errormobile = ObservableField<String>()




   /*  fun isvalidEditField(): Boolean {

        if (first_name.isNullOrEmpty() && first_name.isNullOrEmpty()) {
            errorfirstname.set(FIRST_NAME)
            onError(FIRST_NAME)
            return false
        }
         else if(last_name.isNullOrEmpty() && last_name.isNullOrEmpty())
        {
            errorlastname.set("Enter last name")
            onError("Enter last name")
             return false
        }
         else if(mobile_number.isNullOrEmpty() && mobile_number.isNullOrEmpty())
        {
            errormobile.set("Enter 10 digit mobile number")
            onError("Enter 10 digit mobile number")
            return false
        }

        return true
    }
    fun isvalidLoginEditField(): Boolean {

        if(mobile_number.isNullOrEmpty() && mobile_number.isNullOrEmpty()&& mobile_number!!.length<10)
        {
            errormobile.set("Enter 10 digit mobile number")
            onError("Enter 10 digit mobile number")
            return false
        }

        return true
    }*/


    val errorMessage = MutableLiveData<String>()
    val successMessage = MutableLiveData<String>()

    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }


    val loading = MutableLiveData<Boolean>()


    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }



    private fun onSuccess(message: String) {
        successMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }



}