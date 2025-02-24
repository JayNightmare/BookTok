package com.example.booktok.view.screens

import android.content.Context
import android.net.Uri

fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}
