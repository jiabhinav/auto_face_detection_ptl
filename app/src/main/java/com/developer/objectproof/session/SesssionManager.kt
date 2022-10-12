package com.biocube.biocube_palm.session


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

import com.developer.objectproof.model.ModelUser

import com.google.gson.Gson
import javax.inject.Inject
class SesssionManager @Inject constructor(context: Context?) {

        private val sp_login: SharedPreferences = context!!.getSharedPreferences("info_face", Context.MODE_PRIVATE)
        var sp_editor: SharedPreferences.Editor = sp_login.edit()

        var USER_JSON = "userlogin"
        var USER_Address = "UserAddress"
        var LANGUAGE = "lang"
        var AUTH_TOKEN = ""

    fun sessionLogin(user: ModelUser?) {
        val userdata = Gson().toJson(user)
        sp_editor.putString(USER_JSON, userdata)
        sp_editor.commit()


    }


        fun removeAddress() {
            sp_editor.putString(USER_Address, null)
            sp_editor.commit()
        }

        fun setLang(lang: String) {

            sp_editor.putString(LANGUAGE, lang)
            sp_editor.commit()
        }

        fun getLanguage(): String? {
            return sp_login.getString(LANGUAGE, null)
        }




    fun checkLogin():Boolean {
        if (isLoggedIn() == null)
          return false
        else
         return  true


    }

    fun isLoggedIn(): String? {
        return sp_login.getString(USER_JSON, null)
    }


    fun getValueString(KEY_NAME: String): String? {
        return sp_login.getString(KEY_NAME, "")
    }

    fun logoutSession(activity: Activity):Boolean {
       /* sp_editor.clear()
        sp_editor.commit()
        val logout = Intent(activity, Login::class.java)
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(logout)*/
        return  true
    }

    fun getUser(): ModelUser? {

        try {
            val gson = Gson()
            val list = gson.fromJson(sp_login.getString(USER_JSON, "0"), ModelUser::class.java)
            // list = gson.fromJson(new Gson().toJson(sp_login.getString(USER_JSON, "0")), new TypeToken< UserModel.User>(){}.getType());
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

        }












