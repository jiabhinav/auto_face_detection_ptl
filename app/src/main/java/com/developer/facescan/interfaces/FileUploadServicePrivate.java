package com.developer.facescan.interfaces;


import com.developer.facescan.model.Response;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadServicePrivate {
    @Multipart
    @POST("registration")
    Call<Response> upload(
        @Part("userid") RequestBody userid,
        @Part("hand") RequestBody hand,
        @Part MultipartBody.Part file
    );

    @Multipart
    @POST("authentication")
    Call<Response> authentication(
            @Part("features")RequestBody features,
            @Part("hand") RequestBody hand,
            @Part MultipartBody.Part file
    );

}