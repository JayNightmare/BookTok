package com.example.booktok.viewmodel

import android.content.Context
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
import java.util.Date

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    // StateFlow for all books
    private val _allBooks = repository.allBooks
        .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val allBooks: StateFlow<List<Book>> = _allBooks

    // StateFlow for the selected genre and progress
    private val _selectedGenre = MutableStateFlow<String?>(null)
    private val _selectedProgress = MutableStateFlow<Float?>(null)

    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    val selectedProgress: StateFlow<Float?> = _selectedProgress.asStateFlow()

    // Dynamically update the selected genre and progress
    fun setSelectedGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    // Dynamically update the selected progress
    fun setSelectedProgress(progress: Float?) {
        _selectedProgress.value = progress
    }

    // Selected Books Logic
    private val _selectedBooks = MutableStateFlow<List<Book>>(emptyList())
    val selectedBooks: StateFlow<List<Book>> = _selectedBooks.asStateFlow()

    fun toggleBookSelection(book: Book) {
        val currentSelection = _selectedBooks.value.toMutableList()
        if (currentSelection.contains(book)) {
            currentSelection.remove(book)
        } else {
            currentSelection.add(book)
        }
        _selectedBooks.value = currentSelection
    }

    fun setSelectedBooks(books: List<Book>) {
        _selectedBooks.value = books
    }

    // StateFlow for the currently selected book
    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    // StateFlow for the search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow for the progress sort order
    private val _progressSortOrder = MutableStateFlow<SortOrder?>(null)
    val progressSortOrder: StateFlow<SortOrder?> = _progressSortOrder.asStateFlow()

    // StateFlow for the date range
    private val _startDate = MutableStateFlow<Date?>(null)
    private val startDate: StateFlow<Date?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Date?>(null)
    private val endDate: StateFlow<Date?> = _endDate.asStateFlow()

    // Dynamic list of genres
    val genres = allBooks.map { books ->
        books.mapNotNull { it.genre }.distinct()
    }

    // Load book based on book id
    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            repository.getBookById(bookId).collect { book ->
                _currentBook.value = book
            }
        }
    }

    // Update the current book
    fun updateCurrentBook(book: Book) {
        _currentBook.value = book
    }

    // Update the search query
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setProgressSortOrder(order: SortOrder?) {
        _progressSortOrder.value = order
    }

    // Search For Books
    fun searchBooks(): StateFlow<List<Book>> {
        return combine(
            combine(allBooks, searchQuery, selectedGenre, selectedProgress, progressSortOrder)
            { books, query, genre, progress, sortOrder ->
                FilterParams(books, query, genre, progress, sortOrder)
            },
            combine(startDate, endDate) { start, end ->
                DateRange(start, end)
            },
            dateAddedFilter
        ) { filterParams, dateRange, dateFilter ->

            val now = Date()

            filterParams.books.filter { book ->
                val matchesQuery = filterParams.query.isBlank() ||
                        book.title.contains(filterParams.query, ignoreCase = true) ||
                        book.author.contains(filterParams.query, ignoreCase = true)

                val matchesGenre = filterParams.genre == null ||
                        book.genre.equals(filterParams.genre, ignoreCase = true)

                val matchesProgress = filterParams.progress == null ||
                        (book.progress >= filterParams.progress)

                val matchesStartDate = dateRange.start == null || !book.dateAdded.before(dateRange.start)
                val matchesEndDate = dateRange.end == null || !book.dateAdded.after(dateRange.end)

                val matchesDateAdded = when (dateFilter) {
                    DateAddedFilter.LAST_ADDED -> {
                        val latestDate = filterParams.books.maxByOrNull { it.dateAdded }?.dateAdded
                        book.dateAdded == latestDate
                    }
                    DateAddedFilter.WEEK_AGO -> {
                        val weekAgo = Date(now.time - 7L * 24 * 60 * 60 * 1000) // 7 days ago
                        !book.dateAdded.before(weekAgo)
                    }
                    DateAddedFilter.MONTH_AGO -> {
                        val monthAgo = Date(now.time - 30L * 24 * 60 * 60 * 1000) // 30 days ago
                        !book.dateAdded.before(monthAgo)
                    }
                    else -> true // No filter applied
                }

                matchesQuery && matchesGenre && matchesProgress && matchesStartDate && matchesEndDate && matchesDateAdded
            }.let { filteredBooks ->
                when (dateFilter) {
                    DateAddedFilter.ASCENDING -> filteredBooks.sortedBy { it.dateAdded }
                    DateAddedFilter.DESCENDING -> filteredBooks.sortedByDescending { it.dateAdded }
                    else -> when (filterParams.sortOrder) {
                        SortOrder.ASCENDING -> filteredBooks.sortedBy { it.progress }
                        SortOrder.DESCENDING -> filteredBooks.sortedByDescending { it.progress }
                        else -> filteredBooks
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    // Helper Data Class
    data class FilterParams(
        val books: List<Book>,
        val query: String,
        val genre: String?,
        val progress: Float?,
        val sortOrder: SortOrder?
    )

    // Date Range Data Class
    data class DateRange(
        val start: Date?,
        val end: Date?
    )

    private val _dateAddedFilter = MutableStateFlow<DateAddedFilter?>(null)
    val dateAddedFilter: StateFlow<DateAddedFilter?> = _dateAddedFilter.asStateFlow()

    fun setDateAddedFilter(filter: DateAddedFilter?) {
        _dateAddedFilter.value = filter
    }

    enum class DateAddedFilter {
        ASCENDING,
        DESCENDING,
        LAST_ADDED,
        WEEK_AGO,
        MONTH_AGO
    }

    enum class SortOrder {
        ASCENDING,
        DESCENDING
    }

    fun getBookById(id: Long) = repository.getBookById(id)

    // Add Book
    fun addBook(book: Book) = viewModelScope.launch {
        repository.insert(book)
    }

    // Update Book
    fun updateBook(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    // Delete Book
    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }

    // Share Book Details
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

        // Create an email request
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

        // Send the email
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

    // Share Book List
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

    // Check if the book data being added is unique
    fun isBookUnique(book: Book): Boolean {
        val existingBooks = allBooks.value

        return existingBooks.none { existing ->
            existing.title.equals(book.title, ignoreCase = true) &&
            existing.author.equals(book.author, ignoreCase = true) &&
            existing.id != book.id // Ignore itself if it's being edited
        }
    }
}