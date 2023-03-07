@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)

package com.androiddev.social.timeline.ui

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize10
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize6
import com.androiddev.social.theme.PaddingSize7
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.LinkListener
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.emojiText
import me.saket.swipe.SwipeAction
import social.androiddev.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun TimelineCard(
    alwaysShowButtonBar: Boolean = false,
    ui: UI, replyToStatus: (String, String, String, Int, Set<Uri>) -> Unit,
    boostStatus: (String) -> Unit,
    favoriteStatus: (String) -> Unit,
    state: ModalBottomSheetState?,
    goToConversation: (UI) -> Unit,
    isReplying: (Boolean) -> Unit,
    showInlineReplies: Boolean,
    modifier: Modifier = Modifier,
) {
//    SwipeableActionsBox(
//        startActions = listOf(rocket()),
//        endActions = listOf(reply(), replyAll()),
////        modifier = Modifier.animateItemPlacement()
//    ) {
    Column(

    ) {
        Column(
            modifier
                .padding(
                    bottom = PaddingSize2,
                    start = PaddingSize2,
                    end = PaddingSize2,
                    top = PaddingSize2
                )
        ) {
            val provider = LocalAuthComponent.current.conversationPresenter().get()
            var presenter by remember { mutableStateOf(provider) }
            val submitPresenter = LocalAuthComponent.current.submitPresenter()
            val beforeStatus: List<Status>? =
                presenter.model.conversations[ui.remoteId]?.before

            val before: MutableList<UI>? =
                beforeStatus?.map { it.toStatusDb(FeedType.Home).mapStatus() }
                    ?.toMutableList()
            var showingReplies by remember { mutableStateOf(false) }
            this@Column.AnimatedVisibility(showingReplies && (before?.size ?: 0) > 0) {
                var showParent by remember { mutableStateOf(false) }

                //            if (!showParent)
                //                Parent(if(showParent) "Show Parent" else "Show Replies Only") { showParent = true }
                this@Column.AnimatedVisibility(showParent) {
                    InnerLazyColumn(before, goToConversation = goToConversation)
                }
            }

            UserInfo(ui)
            Row(
                Modifier
                    .padding(bottom = PaddingSizeNone)
                    .wrapContentHeight()
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val (mapping, text) = emojiText(
                        ui.content,
                        ui.mentions,
                        ui.tags,
                        ui.contentEmojis
                    )
                    var clicked by remember(ui) { mutableStateOf(false) }
                    var showReply by remember(ui) { mutableStateOf(false) }
                    if (clicked) {
                        LaunchedEffect(Unit) {
                            state?.hide()
                        }
                    }


                    val uriHandler = LocalUriHandler.current

                    ClickableText(
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = text,
                        onClick = {
                            if (ui.inReplyTo != null) {
                                goToConversation(ui)
                                clicked = false
                            } else {
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
                            }
                        },
                        inlineContent = mapping
                    )

                    ui.imageUrl?.let { ContentImage(it) { clicked = !clicked } }
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
                    AnimatedVisibility(visible = showReply) {
                        var mentions =
                            ui.mentions.map { mention -> mention.username }.toMutableList()

                        mentions.add(ui.userName)
                        mentions = mentions.map { "@${it}" }.toMutableList()
                        Column(modifier = Modifier.padding(top = PaddingSize2)) {
                            UserInput(
                                ui,
                                connection = nestedScrollConnection,
                                onMessageSent = { it, visibility, uris ->
                                    replyToStatus(it, visibility, ui.remoteId, ui.replyCount, uris)
                                },
                                defaultVisiblity = "Direct",
                                participants = mentions.joinToString(" "),
                                showReplies = true,
                                goToConversation = goToConversation
                            )
                        }
                    }
                    AnimatedVisibility(true) {

                        Column() {

                            ButtonBar(
                                ui,
                                ui.replyCount,
                                ui.boostCount,
                                ui.favoriteCount,
                                ui.favorited,
                                ui.boosted,
                                ui.inReplyTo != null,
                                showInlineReplies,
                                onBoost = {
                                    boostStatus(ui.remoteId)
                                },
                                onFavorite = {
                                    favoriteStatus(ui.remoteId)
                                },
                                showReply = showingReplies,
                                onShowReplies = {
                                    showingReplies = !showingReplies
                                    goToConversation(ui)
                                },
                                onReply = {
                                    showReply = !showReply
                                    isReplying(showReply)
                                },
                                goToConversation = {
                                    goToConversation(ui)
                                })
                        }

                    }
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
                        modifier = Modifier
                            .padding(bottom = PaddingSize0_5)
                            .fillMaxWidth(.6f),
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        inlineContent = inlineContentMap
                    )
                    if (ui.directMessage) {
                        DirectMessage(ui.directMessage)
                    }
                    if (ui.boostedBy != null) Boosted(
                        ui.boostedBy,
                        ui.boostedAvatar,
                        ui.boostedEmojis,
                        R.drawable.rocket3
                    )

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        color = colorScheme.secondary,
                        text = ui.userName,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        color = colorScheme.secondary,
                        text = ui.timePosted,
                        style = MaterialTheme.typography.titleMedium,
                    )

                }

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


@Composable
fun uiFrom(account: Account): UI {
    val ui = UI(
        imageUrl = null,
        displayName = account.displayName,
        userName = account.username,
        content = account.note,
        replyCount = 0,
        boostCount = 0,
        favoriteCount = 0,
        timePosted = account.createdAt.toString(),
        boostedBy = null,
        directMessage = false,
        avatar = account.avatar,
        mentions = emptyList(),
        tags = emptyList(),
        contentEmojis = null,
        accountEmojis = account.emojis,
        boostedEmojis = null,
        boostedAvatar = null,
        remoteId = account.id,
        type = FeedType.Home,
        favorited = false,
        boosted = false,
        inReplyTo = null,
    )
    return ui
}