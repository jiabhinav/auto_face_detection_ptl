package com.developer.objectproof.interfaces;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @POST("common/finger")
    Call<ResponseBody> upload(
        @Part("age") RequestBody age,
        @Part("gender") RequestBody gender,
        @Part("id") RequestBody id,
        @Part("name") RequestBody name,
        @Part("time") RequestBody time,
        @Part MultipartBody.Part file
    );
}