package com.androiddev.social.timeline.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.ui.util.VideoPlayer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.shimmer
import kotlinx.coroutines.awaitCancellation
import social.androiddev.firefly.R
import kotlin.math.absoluteValue

@Composable
fun AvatarImage(
    size: Dp = PaddingSize4,
    url: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box() {
        AsyncImage(
            model = url,
            contentDescription = "Content",
            imageLoader = LocalImageLoader.current,
            modifier = modifier
                .clip(shape = RoundedCornerShape(8, 8, 8, 8))
                .clickable { onClick() }
                .placeholder(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        LocalAbsoluteTonalElevation.current + 8.dp
                    ),
                    visible = url == null,
                    shape = RoundedCornerShape(8.dp),
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentImage(
    url: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val scale = remember { mutableStateOf(1.5f) }
    var clicked by remember { mutableStateOf(false) }
    val rotationState = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(1f) }
    val offsetY = remember { mutableStateOf(1f) }


    if (url.firstOrNull()?.contains(".mp4") == true) {
        VideoPlayer(url.first())
    } else {
        if (clicked && url.size > 1) {
            Dialog(
                onDismissRequest = { clicked = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                ),
            ) {
                HorizontalPagerWithOffsetTransition(modifier = Modifier, url.takeLast(8))
            }

        }
        Box(modifier = Modifier.wrapContentSize()) {
            var zoom by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            val minScale = 0.25f
            val maxScale = 3f
            AsyncImage(
                model = url.firstOrNull(),
                contentDescription = "Content",
                imageLoader = LocalImageLoader.current,
                modifier = modifier
                .fillMaxWidth()
//                .aspectRatio(1f)
                    .padding(vertical = 4.dp)
                    .clip(shape = RoundedCornerShape(4, 4, 4, 4))
                    .clickable { clicked = true }
                    .background(Transparent)
                    .graphicsLayer(
                        scaleX = zoom,
                        scaleY = zoom,
                        translationX = offsetX,
                        translationY = offsetY,
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            onGesture = { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(minScale, maxScale)
                                if(zoom > 1) {
                                    offsetX += pan.x * zoom
                                    offsetY += pan.y * zoom
                                }else{
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        )
                    },
                transform = AsyncImagePainter.DefaultTransform,
                onState = { },
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                alpha = DefaultAlpha,
                colorFilter = null,
                filterQuality = DrawScope.DefaultFilterQuality
            )
            if (url.size > 1) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd),
                    painter = painterResource(R.drawable.more),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        }

    }
}


suspend fun ScrollableState.setScrolling(value: Boolean) {
    scroll(scrollPriority = MutatePriority.PreventUserInput) {
        when (value) {
            true -> Unit
            else -> awaitCancellation()
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerWithOffsetTransition(modifier: Modifier = Modifier, urls: List<String>) {
    HorizontalPager(
        count = urls.size,
        // Add 32.dp horizontal padding to 'center' the pages
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier.wrapContentSize()
    ) { page ->
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Transparent,
            ),
            modifier = Modifier
//                .background(Red)
                .graphicsLayer {
                    // Calculate the absolute offset for the current page from the
                    // scroll position. We use the absolute value which allows us to mirror
                    // any effects for both directions
                    val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                    // We animate the scaleX + scaleY, between 85% and 100%
                    lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    ).also { scale ->
                        scaleX = scale
                        scaleY = scale
                    }

                    // We animate the alpha, between 50% and 100%
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
                .wrapContentSize()
        ) {
            Box() {
                var zoom by remember { mutableStateOf(1f) }
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }
                val minScale = 0.25f
                val maxScale = 3f
                AsyncImage(

                    model = urls[page],
                    contentDescription = "Content",
                    imageLoader = LocalImageLoader.current,
                    modifier = Modifier
                        .wrapContentSize()
                        .scale(1f)
//                .aspectRatio(1f)
                        .clickable { }
                        .background(Transparent)
                        .graphicsLayer(
                            scaleX = zoom,
                            scaleY = zoom,
                            translationX = offsetX,
                            translationY = offsetY,
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { _, pan, gestureZoom, _ ->
                                    zoom = (zoom * gestureZoom).coerceIn(minScale, maxScale)
                                    if(zoom > 1) {
                                        offsetX += pan.x * zoom
                                        offsetY += pan.y * zoom
                                    }else{
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            )
                        }
                       ,
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
    }
}
