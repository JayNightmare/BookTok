package com.example.booktok.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.booktok.model.Book
import com.example.booktok.model.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _allBooks = repository.allBooks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val allBooks: StateFlow<List<Book>> = _allBooks

    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _selectedProgress = MutableStateFlow<Float?>(null)

    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    val selectedProgress: StateFlow<Float?> = _selectedProgress.asStateFlow()

    fun setSelectedGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    fun setSelectedProgress(progress: Float?) {
        _selectedProgress.value = progress
    }

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _progressSortOrder = MutableStateFlow<SortOrder?>(null)
    val progressSortOrder: StateFlow<SortOrder?> = _progressSortOrder.asStateFlow()

    // Dynamic list of genres
    val genres = allBooks.map { books ->
        books.mapNotNull { it.genre }.distinct()
    }

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

    fun setProgressSortOrder(order: SortOrder?) {
        _progressSortOrder.value = order
    }

    fun searchBooks(): StateFlow<List<Book>> {
        return combine(allBooks, searchQuery, selectedGenre, selectedProgress, progressSortOrder) { books, query, genre, progress, sortOrder ->
            books.filter { book ->
                val matchesQuery = query.isBlank() || book.title.contains(query, ignoreCase = true) || book.author.contains(query, ignoreCase = true)
                val matchesGenre = genre == null || book.genre.equals(genre, ignoreCase = true)
                val matchesProgress = progress == null || (book.progress >= progress)

                matchesQuery && matchesGenre && matchesProgress
            }.let { filteredBooks ->
                when (sortOrder) {
                    SortOrder.ASCENDING -> filteredBooks.sortedBy { it.progress }
                    SortOrder.DESCENDING -> filteredBooks.sortedByDescending { it.progress }
                    else -> filteredBooks
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    enum class SortOrder {
        ASCENDING,
        DESCENDING
    }

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

    fun shareBookSummary(context: android.content.Context, book: Book, recipientEmail: String = "") {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Book Summary: ${book.title}")
            putExtra(android.content.Intent.EXTRA_TEXT,
                """
        📖 Title: ${book.title}
        ✍️ Author: ${book.author}
        🏷️ Genre: ${book.genre ?: "Not specified"}
        📊 Progress: ${(book.progress * 100).toInt()}% completed
        📄 Pages Read: ${book.pagesRead}/${book.totalPages}
        """.trimIndent()
            )
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share book summary via"))
    }

    fun shareBookList(context: android.content.Context, books: List<Book>, recipientEmail: String = "") {
        val subject = "My Book List from BookTok 📚"
        val bookListText = books.joinToString("\n\n") { book ->
            """
        📖 Title: ${book.title}
        ✍️ Author: ${book.author}
        🏷️ Genre: ${book.genre ?: "Not specified"}
        📊 Progress: ${(book.progress * 100).toInt()}% completed
        📄 Pages Read: ${book.pagesRead}/${book.totalPages}
        """.trimIndent()
        }

        // Use mailto URI for email sharing
        val intent = android.content.Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipientEmail") // Pre-fill recipient if provided
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bookListText)
        }

        // Check if an email app is available
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Send Book List via Email"))
        } else {
            Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveBackgroundUri(context: Context, uri: String) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("background_image_uri", uri).apply()
        Log.d("ViewModel", "Saved backgroundImageUri: $uri")
    }

    fun getBackgroundUri(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("background_image_uri", null)
    }
}