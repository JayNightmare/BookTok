package com.example.booktok.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.booktok.model.Book
import com.example.booktok.model.BookRepository
import com.google.android.gms.drive.query.SortOrder
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
    val allBooks: StateFlow<List<Book>> = _allBooks

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
