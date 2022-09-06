package com.developer.facescan

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imageTable")
class Image (@ColumnInfo(name = "title")val noteTitle :String, @ColumnInfo(name = "mobile")val noteDescription :String, @ColumnInfo(name = "timestamp")val timeStamp :String) {
    @PrimaryKey(autoGenerate = true) var id = 100
}