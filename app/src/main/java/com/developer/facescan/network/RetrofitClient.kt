package com.developer.facescan.network

import com.developer.facescan.utils.AppConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
   private val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()
    
    fun getInstance(): Retrofit{
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL).client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}