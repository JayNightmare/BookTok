package com.example.booktok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.booktok.ui.components.BookItem
import com.example.booktok.ui.components.BookSearchBar
import com.example.booktok.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onBookClick: (Long) -> Unit,
    onAddBook: () -> Unit,
    viewModel: BookViewModel
) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    val selectedGenre by remember { mutableStateOf<String?>(null) }
    val selectedProgress by remember { mutableStateOf<Float?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BookTok") },
                actions = {
                    IconButton(onClick = { 
                        viewModel.shareBookList(context, books) 
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Book List")
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Options")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("By Date Added") },
                            onClick = { /* TODO: Implement sorting */ }
                        )
                        DropdownMenuItem(
                            text = { Text("By Title") },
                            onClick = { /* TODO: Implement sorting */ }
                        )
                        DropdownMenuItem(
                            text = { Text("By Progress") },
                            onClick = { /* TODO: Implement sorting */ }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBook) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            BookSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    viewModel.setSearchQuery(query)
                    viewModel.searchBooks()
                },
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGenre != null,
                    onClick = { viewModel.getBooksByGenre(selectedGenre ?: "") },
                    label = { Text("Genre") }
                )
                FilterChip(
                    selected = selectedProgress != null,
                    onClick = { viewModel.getBooksByProgress(selectedProgress ?: 0f) },
                    label = { Text("Progress") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    BookItem(
                        book = book,
                        onClick = { onBookClick(book.id) }
                    )
                }
            }
        }
    }
}
