package com.developer.facescan.application
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate



class App : Application() {

    companion object
    {

         var context:Context?=null

         var activity: Activity?=null

         var sInstance: App?=null

      /*  @Synchronized
        fun getInstance(): App? {
            return sInstance
        }*/


        @JvmStatic fun getInstance(): App {
            return sInstance as App
        }

        @JvmName("setActivity1")
        fun setActivity(act: Activity?) {
            activity = act
        }

        @JvmName("getActivity1")
        fun getActivity(): Activity? {
            return activity
        }


        @JvmName("setContext1")
        fun setContext(con: Context) {
            context=con
        }

    }


    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        sInstance = this
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }







}