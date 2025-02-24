package com.example.booktok.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.booktok.R

@Composable
fun BackgroundWithContent(
    coverImage: String? = null,
    defaultImageRes: Int = R.drawable.default_background,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    Log.d("BackgroundWithContent", ">> Rendering background with URI: $coverImage")

    val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(coverImage?.let { Uri.parse(it) } ?: defaultImageRes)
                .crossfade(true)
                .placeholder(defaultImageRes)
                .error(defaultImageRes)
                .build()
            )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}
