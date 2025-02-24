package com.example.booktok.ui.components

import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.booktok.model.Book
import java.util.Calendar
import java.util.Locale

@Composable
fun BookForm(
    book: Book,
    onBookChange: (Book) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean,
    onSelectImage: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // State to control DatePicker visibility
    var showDatePicker by remember { mutableStateOf(false) }

    // Separate calendar state to prevent recompositions
    val calendar = remember { Calendar.getInstance().apply { time = book.dateAdded } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // If showError is true, display an error message
        if (showError) {
            Text(
                text = "Please fill all required fields correctly",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Cover Image Picker
        Button(onClick = onSelectImage) {
            Text("Select Book Cover")
        }

        // Enter Title
        OutlinedTextField(
            value = book.title,
            onValueChange = { onBookChange(book.copy(title = it)) },
            label = { Text("Title*") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.title.isEmpty()
        )

        // Enter Author
        OutlinedTextField(
            value = book.author,
            onValueChange = { onBookChange(book.copy(author = it)) },
            label = { Text("Author*") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.author.isEmpty()
        )

        // Enter Genre (optional)
        OutlinedTextField(
            value = book.genre ?: "",
            onValueChange = { onBookChange(book.copy(genre = it.ifEmpty { null })) },
            label = { Text("Genre (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Enter Total Pages
        OutlinedTextField(
            value = book.totalPages.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                onBookChange(book.copy(totalPages = pages))
            },
            label = { Text("Total Pages*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            isError = showError && (book.totalPages <= 0)
        )

        // Enter Pages Read
        OutlinedTextField(
            value = book.pagesRead.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                onBookChange(book.copy(pagesRead = pages))
            },
            label = { Text("Pages Read") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )

        // Enter Date added
        OutlinedTextField(
            value = dateFormatter.format(book.dateAdded),
            onValueChange = {},
            label = { Text("Date Added") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

        // DatePickerDialog
        if (showDatePicker) {
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time
                    onBookChange(book.copy(dateAdded = selectedDate))
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Preview the Selected Image
        book.backgroundImageUri?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .width(150.dp)
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

    }
}