package com.developer.facescan.model

data class ResponseTrain(
    val `data`: Data,
    val message: String,
    val statuscode: String
) {
    data class Data(
        val encodings: String,
        val message: String,
        val output: String
    )
}