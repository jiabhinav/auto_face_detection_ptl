package com.developer.objectproof.network

import com.developer.objectproof.utils.AppConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientPrivate {
   private val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private var httpClient = OkHttpClient.Builder().readTimeout(50,TimeUnit.SECONDS)
        .writeTimeout(50,TimeUnit.SECONDS).callTimeout(5,TimeUnit.MINUTES)
        .connectTimeout(40,TimeUnit.SECONDS)
        .addInterceptor(logging).build()

    fun getInstance(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_PRIVATE).client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}