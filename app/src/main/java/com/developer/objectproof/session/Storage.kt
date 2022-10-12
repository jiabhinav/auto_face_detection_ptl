package com.developer.objectproof.session

interface Storage {
    fun setString(key: String, value: String)
    fun getString(key: String): String
}
