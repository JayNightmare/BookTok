package com.example.booktok.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<Book>

    @Query("SELECT * FROM books WHERE title LIKE :query OR author LIKE :query")
    fun searchBooks(query: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE genre = :genre ORDER BY dateAdded DESC")
    fun getBooksByGenre(genre: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE pagesRead / totalPages >= :progress ORDER BY dateAdded DESC")
    fun getBooksByProgress(progress: Float): Flow<List<Book>>
}
