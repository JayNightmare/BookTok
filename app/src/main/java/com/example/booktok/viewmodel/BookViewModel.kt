package com.example.booktok.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.booktok.model.Book
import com.example.booktok.model.BookRepository
import kotlinx.coroutines.flow.combine

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _selectedProgress = MutableStateFlow<Float?>(null)

    private val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    private val selectedProgress: StateFlow<Float?> = _selectedProgress.asStateFlow()

    fun setSelectedGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    fun setSelectedProgress(progress: Float?) {
        _selectedProgress.value = progress
    }

    val filteredBooks = combine(
        repository.allBooks,
        selectedGenre,
        selectedProgress
    ) { books, genre, progress ->
        books.filter { book ->
            (genre == null || book.genre == genre) &&
            (progress == null || book.pagesRead / book.totalPages >= progress)
        }
    }

    val allBooks = repository.allBooks

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            repository.getBookById(bookId).collect { book ->
                _currentBook.value = book
            }
        }
    }

    fun updateCurrentBook(book: Book) {
        _currentBook.value = book
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchBooks() = repository.searchBooks(_searchQuery.value)

    fun addBook(book: Book) = viewModelScope.launch {
        repository.insert(book)
    }

    fun getBookById(id: Long) = repository.getBookById(id)

    fun updateBook(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }

    fun updateReadingProgress(book: Book, pagesRead: Int) = viewModelScope.launch {
        val updatedBook = book.copy(pagesRead = pagesRead)
        repository.update(updatedBook)
    }

    fun shareBookSummary(context: android.content.Context, book: Book) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Book Summary: ${book.title}")
            putExtra(android.content.Intent.EXTRA_TEXT, """
                Book Title: ${book.title}
                Author: ${book.author}
                Genre: ${book.genre ?: "Not specified"}
                Progress: ${(book.progress * 100).toInt()}%
                Pages Read: ${book.pagesRead}/${book.totalPages}
            """.trimIndent())
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share book summary via"))
    }

    fun shareBookList(context: android.content.Context, books: List<Book>) {
        val bookListText = books.joinToString("\n\n") { book ->
            """
            Title: ${book.title}
            Author: ${book.author}
            Progress: ${(book.progress * 100).toInt()}%
            """.trimIndent()
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Book List")
            putExtra(android.content.Intent.EXTRA_TEXT, bookListText)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share book list via"))
    }
}
