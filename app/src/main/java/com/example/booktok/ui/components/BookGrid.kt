package com.example.booktok.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booktok.model.Book

@Composable
fun BookGrid(
    books: List<Book>,
    onBookClick: (Long) -> Unit,
    selectedBooks: List<Book>,
    onBookLongClick: (Book) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "My Book Library",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columns grid
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(books) { book ->
                Log.d("BookGrid", ">> Displaying book: $book")
                BookItem(
                    book = book,
                    onClick = { onBookClick(book.id) },
                    isSelected = selectedBooks.contains(book),
                    onLongClick = { onBookLongClick(book) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f), // Maintain card aspect ratio
                )
            }
        }
    }
}
