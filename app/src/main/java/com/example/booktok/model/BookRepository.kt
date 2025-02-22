package com.example.booktok.model

import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun insert(book: Book) = bookDao.insert(book)

    suspend fun update(book: Book) = bookDao.update(book)

    suspend fun delete(book: Book) = bookDao.delete(book)

    fun getBookById(id: Long): Flow<Book> = bookDao.getBookById(id)
}
