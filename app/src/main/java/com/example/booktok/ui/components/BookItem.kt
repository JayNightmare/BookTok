package com.example.booktok.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.booktok.R
import com.example.booktok.model.Book

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Log.d("Selected Book", ">> Selected Book: $isSelected for ${book.title}")

    // Use the book id as a key to tie recomposition to that book's data
    key(book.id) {
        Card(
            modifier = modifier
                .width(150.dp)
                .height(250.dp)
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { onLongClick() }
                ),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val bitmap = produceState<Bitmap?>(initialValue = null, key1 = book.coverImage) {
                    value = book.coverImage?.let { data ->
                        BitmapFactory.decodeByteArray(data, 0, data.size)
                    }
                }

                bitmap.value?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Book Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Image(
                    painter = rememberAsyncImagePainter(model = R.drawable.default_background),
                    contentDescription = "Default Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )


                // Highlight Selected Book
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(red = 0x00, green = 0x00, blue = 0x00, alpha = 0x87))  // Semi-transparent green overlay
                    )

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.Green,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .border(width = 1.dp, color = Color.Green, shape = CircleShape)
                    )
                }

                // Gradient overlay and text remain as before
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 1f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.genre ?: "Not specified",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = remember(book.pagesRead, book.totalPages) {
                        derivedStateOf { book.pagesRead / book.totalPages.toFloat() }
                    }

                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = "${(book.progress * 100).toInt()}% completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
