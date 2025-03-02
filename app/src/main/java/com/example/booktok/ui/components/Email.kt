package com.example.booktok.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booktok.model.Book

@Composable
fun EmailInputDialog(
    title: String,
    selectedBooks: List<Book>? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    selectedBook: Book?
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Recipient Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display the selected books list
                Text(text = "Books to Share:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedBooks != null) {
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(selectedBooks) { book ->
                            Text("📖 ${book.title} by ${book.author}")
                        }
                    }
                } else if (selectedBook != null) {
                    Text("📖 ${selectedBook.title} by ${selectedBook.author}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(email) },
                enabled = email.isNotBlank()  // Disable the button if email is empty
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
