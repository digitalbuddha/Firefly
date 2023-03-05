package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.ui.util.VideoPlayer

@Composable
fun AvatarImage(
    size: Dp = PaddingSize4,
    url: String? = "https://placekitten.com/300/300",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box() {
        AsyncImage(
            model = url,
            contentDescription = "Content",
            imageLoader = LocalImageLoader.current,
            modifier = modifier
                .clip(CircleShape)
                .clickable { onClick() }
                .size(size),
            transform = AsyncImagePainter.DefaultTransform,
            onState = { },
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            alpha = DefaultAlpha,
            colorFilter = null,
            filterQuality = DrawScope.DefaultFilterQuality
        )
    }

}

@Composable
fun ContentImage(
    url: String = "https://placekitten.com/302/302",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    if (url.contains(".mp4")) {
        VideoPlayer(url)
    } else {
        AsyncImage(
            model = url,
            contentDescription = "Content",
            imageLoader = LocalImageLoader.current,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(0.dp)
                .clickable { onClick() }
                .background(Color.Transparent),
            transform = AsyncImagePainter.DefaultTransform,
            onState = { },
            alignment = Alignment.Center,
            contentScale = ContentScale.FillWidth,
            alpha = DefaultAlpha,
            colorFilter = null,
            filterQuality = DrawScope.DefaultFilterQuality
        )
    }
}

