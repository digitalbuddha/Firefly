package com.androiddev.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun Avatar(size:Dp = 50.dp, url:String = "https://placekitten.com/300/300"){
    AsyncImage(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Green)
            .size(size)
            ,
        alignment = Alignment.CenterStart,

        model = url,
        contentScale = ContentScale.Fit,
        contentDescription = "Translated description of what the image contains"
    )
}
