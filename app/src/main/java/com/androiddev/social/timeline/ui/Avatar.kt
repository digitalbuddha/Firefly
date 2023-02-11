package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.androiddev.social.timeline.ui.theme.Pink40

@Composable
fun Avatar(
    size: Dp = 36.dp,
    url: String = "https://placekitten.com/300/300",
    showIcon: Boolean = false
) {
    Box() {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Green)
                .size(size)
                .border(1.dp, Pink40),
            alignment = Alignment.CenterStart,
            model = url,
            contentScale = ContentScale.Fit,
            contentDescription = "Translated description of what the image contains"
        )
        if (showIcon)
            AsyncImage(
                modifier = Modifier
                    .padding(start = 36.dp, top = 36.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
                    .size(size / 2.5f)
                    .border(1.dp, Pink40),
                alignment = Alignment.BottomEnd,
                model = "https://placekitten.com/303/303",
                contentScale = ContentScale.Fit,
                contentDescription = "Translated description of what the image contains"
            )
    }

}

@Composable
fun ContentImage(url: String = "https://placekitten.com/302/302") {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        alignment = Alignment.Center,
        model = url,
        contentScale = ContentScale.FillHeight,
        contentDescription = "Translated description of what the image contains"
    )
}

