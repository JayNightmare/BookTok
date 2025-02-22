package com.example.booktok.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@TypeConverters(Converters::class)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val genre: String? = null,
    val dateAdded: Date = Date(),
    val pagesRead: Int = 0,
    val totalPages: Int = 0
) {
    val progress: Float
        get() = if (totalPages > 0) pagesRead.toFloat() / totalPages else 0f
}
