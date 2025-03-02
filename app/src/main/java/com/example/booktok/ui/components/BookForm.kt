package com.example.booktok.ui.components

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booktok.model.Book
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BookForm(
    book: Book,
    onBookChange: (Book) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean,
    onSelectImage: () -> Unit,
    isEditing: Long?
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Focus Requesters for each field
    val authorFocus = remember { FocusRequester() }
    val genreFocus = remember { FocusRequester() }
    val pagesFocus = remember { FocusRequester() }
    val pagesReadFocus = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Show error message if applicable
        if (showError) {
            Text(
                text = "Please fill all required fields correctly",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Preview the Selected Image
        book.coverImage?.let {
            val bitmap by remember(book.coverImage) {
                mutableStateOf(
                    book.coverImage.let { byteArray ->
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    }
                )
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Book Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        // Cover Image Picker
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            // Cover Image Picker Button
            Button(onClick = onSelectImage) {
                Text(if (isEditing != null) "Change Book Cover" else "Select Book Cover")
            }

            // Remove Cover Image Button
            if (isEditing != null) {
                IconButton( onClick = { onBookChange(book.copy(coverImage = null)) } )
                { Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Cover Image"
                )}
            }
        }

        // Title Field
        OutlinedTextField(
            value = book.title,
            onValueChange = { onBookChange(book.copy(title = it)) },
            label = { Text("Title*") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && book.title.isEmpty(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { authorFocus.requestFocus() }),
            singleLine = true
        )

        // Author Field
        OutlinedTextField(
            value = book.author,
            onValueChange = { onBookChange(book.copy(author = it)) },
            label = { Text("Author*") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(authorFocus),
            isError = showError && book.author.isEmpty(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { genreFocus.requestFocus() }),
            singleLine = true
        )

        // Genre Field
        OutlinedTextField(
            value = book.genre ?: "",
            onValueChange = { onBookChange(book.copy(genre = it.ifEmpty { null })) },
            label = { Text("Genre (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(genreFocus),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { pagesFocus.requestFocus() }),
            singleLine = true
        )

        // Total Pages Field
        OutlinedTextField(
            value = book.totalPages.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                onBookChange(book.copy(totalPages = pages))
            },
            label = { Text("Total Pages*") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(pagesFocus),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { pagesReadFocus.requestFocus() }),
            isError = showError && (book.totalPages <= 0),
            singleLine = true
        )

        // Pages Read Field
        OutlinedTextField(
            value = book.pagesRead.toString(),
            onValueChange = { value ->
                val pages = value.toIntOrNull() ?: 0
                if (pages in 0..book.totalPages) {
                    onBookChange(book.copy(pagesRead = pages))
                }
            },
            label = { Text("Pages Read") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(pagesReadFocus),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = book.pagesRead > book.totalPages,
            singleLine = true
        )

        // Date Added Field
        ClickableTextField(
            text = dateFormatter.format(book.dateAdded),
            label = "Date Added",
            onClick = {
                // Show DatePickerDialog
                showDatePickerDialog(context) { selectedDate ->
                    onBookChange(book.copy(dateAdded = selectedDate))
                }
            }
        )
    }
}

fun showDatePickerDialog(context: Context, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
