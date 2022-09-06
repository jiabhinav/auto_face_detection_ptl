package com.developer.facescan.room_db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "embeddingTable")
class Embedding(
    @ColumnInfo(name = "userid")val userid :String, @ColumnInfo(name = "emdbedding")val emdbedding :String)
{
    @PrimaryKey(autoGenerate = true) var id = 0
}

