package com.androiddev.social.timeline.ui

import android.text.Spanned
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.androiddev.social.R
import com.androiddev.social.theme.*
import com.androiddev.social.timeline.data.LinkListener
import com.androiddev.social.timeline.data.setClickableText
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.model.parseAsMastodonHtml
import com.androiddev.social.timeline.ui.model.toAnnotatedString
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.TimelineCard(ui: UI) {
    SwipeableActionsBox(
        startActions = listOf(rocket()),
        endActions = listOf(reply(), replyAll()),
//        modifier = Modifier.animateItemPlacement()
    ) {
        Column(
            Modifier
                .background(colorScheme.surface.copy(alpha = .99f))
                .padding(bottom = PaddingSize2, start = PaddingSize2, end = PaddingSize2, top = PaddingSize2)
        ) {
            DirectMessage(ui.directMessage)
            Boosted(ui.boostedBy, ui.boostedAvatar)
            UserInfo(ui)
            Row(Modifier.padding(bottom = PaddingSizeNone)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val parseAsMastodonHtml: Spanned = ui.content.parseAsMastodonHtml()
                    println(parseAsMastodonHtml)
                    val prettyText = setClickableText(
                        parseAsMastodonHtml,
                        ui.mentions,
                        ui.tags,
                        empty
                    )
                    val uriHandler = LocalUriHandler.current

                    val mapping by remember(ui) { mutableStateOf(mutableMapOf<String, InlineTextContent>()) }
                    val linkColor = colorScheme.primary
                    val text by remember(ui) {
                        mutableStateOf(
                            prettyText.toAnnotatedString(
                                linkColor,
                                ui.contentEmojis,
                                mapping
                            )
                        )
                    }
                    var clicked by remember(ui) { mutableStateOf(false) }
                    var showReply by remember(ui) { mutableStateOf(false) }

                    ClickableText(
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = text,
                        onClick = {
                            clicked = !clicked
                            if (!clicked && showReply) showReply = false
                            text.getStringAnnotations(
                                tag = "URL", start = it,
                                end = it
                            )
                                .firstOrNull()?.let { annotation ->
                                    // If yes, we log its value
                                    uriHandler.openUri(annotation.item)
                                    Log.d("Clicked URL", annotation.item)
                                }
                        },
                        inlineContent = mapping
                    )
                    ui.imageUrl?.let { ContentImage(it, clicked) { clicked = !clicked } }
                    val toolbarHeight = PaddingSize6
                    val toolbarHeightPx =
                        with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }
                    val toolbarOffsetHeightPx =

                        remember(ui) { mutableStateOf(0f) }
                    val nestedScrollConnection = remember(ui) {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                val delta = available.y
                                val newOffset = toolbarOffsetHeightPx.value + delta
                                toolbarOffsetHeightPx.value =
                                    newOffset.coerceIn(-toolbarHeightPx, 0f)
                                return Offset.Zero
                            }
                        }
                    }

                    AnimatedVisibility(visible = clicked) {
                        Column {
                            ButtonBar(ui.replyCount, ui.boostCount) {
                                showReply = !showReply
                            }
                        }
                    }
                    AnimatedVisibility(visible = showReply) {
                        UserInput(connection = nestedScrollConnection,
                            onMessageSent = {
                                it.length
                            }
                        )
                    }

                    Divider(
                        Modifier.padding(top = PaddingSize2),
                        color = Color.Gray.copy(alpha = .5f)
                    )
                }
            }
        }


    }
}



@Composable
fun UserInfo(ui: UI) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = PaddingSize1),
        horizontalArrangement = Arrangement.Start
    ) {
        ui.avatar?.let { AvatarImage(PaddingSize7, it) }
        ui.accountEmojis?.let {
            val (inlineContentMap, text) = inlineEmojis(
                ui.displayName,
                it
            )

            Column(Modifier.padding(start = PaddingSize1)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorScheme.secondary,
                        modifier = Modifier.padding(bottom = PaddingSize0_5).fillMaxWidth(.6f),
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        inlineContent = inlineContentMap
                    )
                    Text(
                        color = colorScheme.secondary,
                        text = ui.timePosted,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    color = colorScheme.secondary,
                    text = ui.userName,
                    style = MaterialTheme.typography.titleSmall,
                )
            }

        }


    }
}

@Composable
fun rocket() = SwipeAction(
    icon = {
        androidx.compose.foundation.Image(
            modifier = Modifier.size(PaddingSize10),
            painter = painterResource(R.drawable.rocket3),
            contentDescription = "",
            colorFilter = ColorFilter.tint(colorScheme.tertiary)
        )
    },
    background = colorScheme.tertiaryContainer,
    onSwipe = { }
)

@Composable
fun reply() = SwipeAction(
    icon = {
        androidx.compose.foundation.Image(
            modifier = Modifier.size(PaddingSize10),
            painter = painterResource(R.drawable.reply),
            contentDescription = "",
            colorFilter = ColorFilter.tint(colorScheme.tertiary)
        )
    },
    background = colorScheme.tertiaryContainer,
    onSwipe = { }
)

@Composable
fun replyAll() = SwipeAction(
    icon = {
        androidx.compose.foundation.Image(
            modifier = Modifier.size(PaddingSize10),
            painter = painterResource(R.drawable.reply_all),
            contentDescription = "",
            colorFilter = ColorFilter.tint(colorScheme.tertiary)
        )
    },
    background = colorScheme.tertiaryContainer,
    isUndo = true,
    onSwipe = { },
)

val empty = object : LinkListener {
    override fun onViewTag(tag: String) {
        TODO("Not yet implemented")
    }

    override fun onViewAccount(id: String) {
        TODO("Not yet implemented")
    }

    override fun onViewUrl(url: String) {
        TODO("Not yet implemented")
    }
}

@Composable
fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onClick: (Int) -> Unit,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
    val layoutResult = remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick(layoutResult.getOffsetForPosition(pos))
            }
        }
    }

    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
        inlineContent = inlineContent
    )
}