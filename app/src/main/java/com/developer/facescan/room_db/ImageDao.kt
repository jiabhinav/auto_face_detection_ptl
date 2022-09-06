package com.developer.facescan

import androidx.lifecycle.LiveData
import androidx.room.*
import com.developer.facescan.room_db.Embedding

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note :Image)

   /* @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmbedding(note :Embedding)*/

    @Update
    suspend fun update(note: Image)

    @Delete
    suspend fun delete(note: Image)

    @Query("Select * from imageTable order by id ASC")
    fun getAllNotes(): LiveData<List<Image>>

  /*  @Query("Select * from embeddingTable order by id ASC")
    fun getEmbedding(): LiveData<List<Embedding>>*/




}