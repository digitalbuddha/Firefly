package com.androiddev.social.timeline.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.ui.util.VideoPlayer
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import social.androiddev.R

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
                .clip(shape = RoundedCornerShape(8, 8, 8, 8))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentImage(
    url: String = "https://placekitten.com/302/302",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val scale = remember { mutableStateOf(1.5f) }
    var clicked by remember { mutableStateOf(false) }
    val rotationState = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(1f) }
    val offsetY = remember { mutableStateOf(1f) }


    if (url.contains(".mp4")) {
        VideoPlayer(url)
    } else {
        if (clicked)
            Dialog(
                onDismissRequest = { clicked = false },
                content = {
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { },
                                onDoubleClick = {
                                    if (scale.value >= 2f) {
                                        scale.value = 1f
                                        offsetX.value = 1f
                                        offsetY.value = 1f
                                    } else scale.value = 3f
                                },
                            )
                            .pointerInput(Unit) {
                                if (true) {
                                    forEachGesture {
                                        awaitPointerEventScope {
                                            awaitFirstDown()
                                            do {
                                                val event = awaitPointerEvent()
                                                scale.value *= event.calculateZoom()
                                                if (scale.value > 1) {
                                                    null?.run {
                                                        coroutineScope.launch {
                                                            setScrolling(false)
                                                        }
                                                    }
                                                    val offset = event.calculatePan()
                                                    offsetX.value += offset.x
                                                    offsetY.value += offset.y
                                                    rotationState.value += event.calculateRotation()
                                                    null?.run {
                                                        coroutineScope.launch {
                                                            setScrolling(true)
                                                        }
                                                    }
                                                } else {
                                                    scale.value = 1f
                                                    offsetX.value = 1f
                                                    offsetY.value = 1f
                                                }
                                            } while (event.changes.any { it.pressed })
                                        }
                                    }
                                }
                            }

                    ) {

                        AsyncImage(
                            model = url,
                            contentDescription = "Content",
                            imageLoader = LocalImageLoader.current,
                            modifier = modifier
                                .graphicsLayer {
                                    if (true) {
                                        scaleX = maxOf(1f, minOf(3f, scale.value))
                                        scaleY = maxOf(1f, minOf(3f, scale.value))
                                        if (false) {
                                            rotationZ = rotationState.value
                                        }
                                        translationX = offsetX.value
                                        translationY = offsetY.value
                                    }
                                }
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .padding(0.dp)
                                .clip(shape = RoundedCornerShape(4, 4, 4, 4))
//                                .clickable { clicked = false }
                                .background(Color.Transparent),
                            transform = AsyncImagePainter.DefaultTransform,
                            onState = { },
                            alignment = Alignment.Center,
                            contentScale = ContentScale.FillWidth,
                            alpha = DefaultAlpha,
                            colorFilter = null,
                            filterQuality = DrawScope.DefaultFilterQuality
                        )
                        Image(
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(0f)
                                .align(Alignment.TopEnd)
                                .clickable { clicked = false }
                            ,
                            painter = painterResource(R.drawable.close),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            )
        AsyncImage(
            model = url,
            contentDescription = "Content",
            imageLoader = LocalImageLoader.current,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(0.dp)
                .clip(shape = RoundedCornerShape(4, 4, 4, 4))
                .clickable { clicked = true }
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


@ExperimentalFoundationApi
@Composable
fun ZoomableImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    imageAlign: Alignment = Alignment.Center,
    shape: Shape = RectangleShape,
    maxScale: Float = 1f,
    minScale: Float = 3f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false,
    isZoomable: Boolean = true,
    scrollState: ScrollableState? = null
) {
    val coroutineScope = rememberCoroutineScope()

    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(1f) }
    val offsetY = remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* NADA :) */ },
                onDoubleClick = {
                    if (scale.value >= 2f) {
                        scale.value = 1f
                        offsetX.value = 1f
                        offsetY.value = 1f
                    } else scale.value = 3f
                },
            )
            .pointerInput(Unit) {
                if (isZoomable) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                scale.value *= event.calculateZoom()
                                if (scale.value > 1) {
                                    scrollState?.run {
                                        coroutineScope.launch {
                                            setScrolling(false)
                                        }
                                    }
                                    val offset = event.calculatePan()
                                    offsetX.value += offset.x
                                    offsetY.value += offset.y
                                    rotationState.value += event.calculateRotation()
                                    scrollState?.run {
                                        coroutineScope.launch {
                                            setScrolling(true)
                                        }
                                    }
                                } else {
                                    scale.value = 1f
                                    offsetX.value = 1f
                                    offsetY.value = 1f
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                }
            }

    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
                .align(imageAlign)
                .graphicsLayer {
                    if (isZoomable) {
                        scaleX = maxOf(maxScale, minOf(minScale, scale.value))
                        scaleY = maxOf(maxScale, minOf(minScale, scale.value))
                        if (isRotation) {
                            rotationZ = rotationState.value
                        }
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
                }
        )
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
