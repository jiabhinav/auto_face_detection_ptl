package com.developer.facescan.model

import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("message")
    var message: String,
    @SerializedName("output")
    var output: String,
    @SerializedName("score")
    var score: String,
    @SerializedName("status")
    var status: String,
    @SerializedName("user")
    var username: String,

    @SerializedName("encodings")
     var encodings: String
)