package com.example.booktok.view.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BackgroundPicker(
    onImageSelected: (String) -> Unit, // Callback to update UI
    context: Context,
    saveBackgroundUri: (Context, String) -> Unit, // Function to persist URI
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Persist permission for future use
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Save the URI to SharedPreferences or DB
            saveBackgroundUri(context, selectedUri.toString())

            // Update UI by calling the callback
            onImageSelected(selectedUri.toString())
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("Select Background Image")
    }
}
