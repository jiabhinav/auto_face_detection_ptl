package com.developer.objectproof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModal(application: Application) : AndroidViewModel(application) {
    val allImages: LiveData<List<Image>>
    val repository: ImageRepository

    init {
        val dao = ImageDatabase.getDatabase(application).getImagesDao()
        repository = ImageRepository(dao)
        allImages = repository.allImages
    }

    fun deleteImage(image: Image) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(image)
    }

    fun updateImage(image: Image) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(image)
    }

    fun addImages(image: Image) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(image)
    }
}