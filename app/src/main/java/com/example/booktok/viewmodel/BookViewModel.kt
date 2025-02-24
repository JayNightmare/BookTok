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
import com.example.booktok.model.network.EmailAddress
import com.example.booktok.model.network.EmailApi
import com.example.booktok.model.network.EmailContent
import com.example.booktok.model.network.EmailRequest
import com.example.booktok.model.network.Personalization
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _allBooks = repository.allBooks
        .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val allBooks: StateFlow<List<Book>> = _allBooks

    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _selectedProgress = MutableStateFlow<Float?>(null)

    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    val selectedProgress: StateFlow<Float?> = _selectedProgress.asStateFlow()

    private fun loadBookImageUris() {
        viewModelScope.launch {
            repository.allBooks.collect { bookList ->
                bookList.forEach { book ->
                    Log.d("DEBUG", ">> Book: ${book.title}, URI: ${book.backgroundImageUri}")
                }
            }
        }
    }

    init {
        loadBookImageUris() // Ensure this is called
    }

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

    fun shareBookSummary(context: Context, book: Book, recipientEmail: String) {
        val subject = "Book Summary: ${book.title}"
        val body = """
        📖 Title: ${book.title}
        ✍️ Author: ${book.author}
        🏷️ Genre: ${book.genre ?: "Not specified"}
        📊 Progress: ${(book.progress * 100).toInt()}% completed
        📄 Pages Read: ${book.pagesRead}/${book.totalPages}
        📆 Date Added: ${book.dateAdded}
    """.trimIndent()

        val emailRequest = EmailRequest(
            personalizations = listOf(
                Personalization(
                    to = listOf(EmailAddress(email = recipientEmail))
                )
            ),
            from = EmailAddress(email = "booktok.app.mad@gmail.com", name = "BookTok App"),
            subject = subject,
            content = listOf(EmailContent(type = "text/plain", value = body))
        )

        // Log the email request payload
        Log.d("DEBUG", "EmailRequest Payload: ${Gson().toJson(emailRequest)}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EmailApi.retrofitService.sendEmail(emailRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Book summary sent successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(context, "Failed to send email: $errorMsg", Toast.LENGTH_LONG).show()
                        Log.e("DEBUG", ">> Failed to send email: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error sending email: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("DEBUG", ">> Exception: ${e.message}")
                }
            }
        }
    }

    fun shareBookList(context: Context, books: List<Book>, recipientEmail: String) {
        val subject = "My Book List from BookTok 📚"
        val bookListText = books.joinToString("\n\n") { book ->
            """
        📖 Title: ${book.title}
        ✍️ Author: ${book.author}
        🏷️ Genre: ${book.genre ?: "Not specified"}
        📊 Progress: ${(book.progress * 100).toInt()}% completed
        📄 Pages Read: ${book.pagesRead}/${book.totalPages}
        📆 Date Added: ${book.dateAdded}
        """.trimIndent()
        }

        val emailRequest = EmailRequest(
            personalizations = listOf(
                Personalization(
                    to = listOf(EmailAddress(email = recipientEmail))
                )
            ),
            from = EmailAddress(email = "booktok.app.mad@gmail.com", name = "BookTok App"),
            subject = subject,
            content = listOf(EmailContent(type = "text/plain", value = bookListText))
        )

        // Log the email request payload
        Log.d("DEBUG", "EmailRequest Payload: ${Gson().toJson(emailRequest)}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EmailApi.retrofitService.sendEmail(emailRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Book list sent successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(context, "Failed to send email: $errorMsg", Toast.LENGTH_LONG).show()
                        Log.e("DEBUG", ">> Failed to send email: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error sending email: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("DEBUG", ">> Exception: ${e.message}")
                }
            }
        }
    }

}