package com.developer.objectproof.interfaces;


import com.developer.objectproof.model.Response;
import com.developer.objectproof.model.ResponseTrain;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
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

    @Headers("Content-Type: application/json")
 //  @Multipart
    @POST("face-training")
    Call<ResponseTrain> face_training(
            @Body JsonObject meta
    );

    @Headers("Content-Type: application/json")
    @POST("face-recognition")
    Call<ResponseTrain> authentication(
            @Body JsonObject meta
    );

}