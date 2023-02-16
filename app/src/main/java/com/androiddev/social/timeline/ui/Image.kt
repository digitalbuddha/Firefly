package com.androiddev.social.timeline.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun Image(
    size: Dp = 36.dp,
    url: String = "https://placekitten.com/300/300",
    showIcon: Boolean = false
) {
    Box() {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
//                .background(Color.Green)
                .size(size)
//                .border(1.dp, Pink40)
            ,
            alignment = Alignment.CenterStart,
            model = url,
            contentScale = ContentScale.FillBounds,
            contentDescription = "Translated description of what the image contains"
        )
    }

}

@Composable
fun ContentImage(url: String = "https://placekitten.com/302/302", clicked: Boolean, onClick: () -> Unit) {

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth(.99f)
            .aspectRatio(1f)
            .padding(8.dp)
            .clickable {
                      onClick
            },
        alignment = Alignment.Center,
        model = url,
        contentScale = ContentScale.FillHeight,
        contentDescription = "Translated description of what the image contains"
    )
}

