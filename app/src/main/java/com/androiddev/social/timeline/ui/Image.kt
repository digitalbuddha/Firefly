package com.androiddev.social.timeline.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.ui.util.VideoPlayer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.shimmer
import kotlinx.coroutines.awaitCancellation
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.zoomable
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableState
import social.androiddev.firefly.R

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
        if (clicked) {
            Dialog(
                onDismissRequest = { clicked = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true
                ),
            ) {
                HorizontalPagerWithOffsetTransition(
                    modifier = Modifier,
                    url.map { MediaItem.NormalSizedRemoteImage(url = it) })
            }

        }
        Box(modifier = Modifier.wrapContentSize()) {
                AsyncImage(
                    model = url.firstOrNull(),
                    contentDescription = "Content",
                    imageLoader = LocalImageLoader.current,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(shape = RoundedCornerShape(4, 4, 4, 4))
                        .clickable { clicked = !clicked }
                        .background(Transparent),
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
fun HorizontalPagerWithOffsetTransition(
    modifier: Modifier = Modifier,
    urls: List<MediaItem.NormalSizedRemoteImage>
) {
    MediaViewerScreen(mediaItems = urls)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun MediaViewerScreen(mediaItems: List<MediaItem>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { CloseNavIconButton() }
            )
        }
    ) { contentPadding ->
        val pagerState = rememberPagerState(initialPage = 0)
        androidx.compose.foundation.pager.HorizontalPager(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            state = pagerState,
            pageCount = mediaItems.size,
            beyondBoundsPageCount = 1,
            pageSpacing = 16.dp,
        ) { pageNum ->
            MediaPage(
                modifier = Modifier.fillMaxSize(),
                model = mediaItems[pageNum]
            )
        }
    }
}

@Composable
private fun CloseNavIconButton() {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
    IconButton(onClick = { backDispatcher.onBackPressed() }) {
        Icon(Icons.Rounded.Close, contentDescription = "Go back")
    }
}

@Composable
private fun MediaPage(
    model: MediaItem,
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberZoomableState()
    NormalSizedRemoteImage(
        viewportState,
        model as MediaItem.NormalSizedRemoteImage,
        modifier,
    )
}


sealed interface MediaItem {
    val url: String


    data class NormalSizedRemoteImage(
        override val url: String
    ) : MediaItem

}

@Composable
fun NormalSizedRemoteImage(
    viewportState: ZoomableState,
    model: MediaItem.NormalSizedRemoteImage,
    modifier: Modifier = Modifier,
) {
    ZoomableAsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(model.url)
            .crossfade(true)
            .build(),
        contentDescription = null,
    )
}