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
import coil.compose.AsyncImage
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize4

@Composable
fun AvatarImage(
    size: Dp = PaddingSize4,
    url: String? = "https://placekitten.com/300/300"
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
            .padding(PaddingSize1)
            .clickable {
                      onClick
            },
        alignment = Alignment.Center,
        model = url,
        contentScale = ContentScale.Inside,
        contentDescription = "Translated description of what the image contains"
    )
}

