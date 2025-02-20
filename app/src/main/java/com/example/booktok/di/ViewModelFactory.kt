package com.example.booktok.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.booktok.data.BookDao
import com.example.booktok.data.BookRepository
import com.example.booktok.ui.viewmodels.BookViewModel

class ViewModelFactory(
    private val bookDao: BookDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(BookRepository(bookDao)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
