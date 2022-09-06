package com.developer.facescan

import androidx.lifecycle.LiveData

class ImageRepository(private val ImagesDao: ImageDao) {

    val allImages: LiveData<List<Image>> = ImagesDao.getAllNotes()

    suspend fun insert(note: Image) {
        ImagesDao.insert(note)
    }
    suspend fun delete(note: Image){
        ImagesDao.delete(note)
    }

    suspend fun update(note: Image){
        ImagesDao.update(note)
    }
}