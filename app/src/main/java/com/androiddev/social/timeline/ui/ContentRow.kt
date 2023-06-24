@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)

package com.androiddev.social.timeline.ui

import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize10
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.theme.PaddingSize6
import com.androiddev.social.theme.PaddingSize7
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.theme.ThickLg
import com.androiddev.social.theme.ThickSm
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.LinkListener
import com.androiddev.social.timeline.ui.model.PollHashUI
import com.androiddev.social.timeline.ui.model.PollUI
import com.androiddev.social.timeline.ui.model.UI
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.shimmer
import me.saket.swipe.SwipeAction
import social.androiddev.firefly.R
import java.lang.Integer.min
import java.net.URI
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimelineCard(
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    ui: UI?,
    account: Account?,
    replyToStatus: (String, String, String, Int, Set<Uri>) -> Unit,
    boostStatus: (remoteId: String, boosted: Boolean) -> Unit,
    favoriteStatus: (remoteId: String, favourited: Boolean) -> Unit,
    goToConversation: (UI) -> Unit,
    onReplying: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit = { a, b -> },
    onVote: (statusId: String, pollId: String, choices: List<Int>) -> Unit,
) {

    val urlHandlerMediator = LocalUserComponent.current.urlHandlerMediator()
    Column(
        modifier
            .padding(
                bottom = PaddingSize1,
                start = PaddingSize1,
                end = PaddingSize1,
                top = PaddingSize1
            ),
    ) {

        var showReply by remember(ui) { mutableStateOf(false) }
        UserInfo(ui, goToProfile, onProfileClick = onProfileClick)
        Column {
            Row(
                Modifier
                    .padding(bottom = PaddingSize1),
            ) {
                ui?.let { status ->
                    val magicNumber = 2
                    repeat(min(magicNumber , status.replyIndention)) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colorScheme.onSurface
                            )
                        )
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .padding(end = PaddingSize0_5)
                                .width(ThickSm)
                                .background(color = colorScheme.onSurface)
                        )
                    }
                    if (status.replyIndention > magicNumber) {
                        Text(
                            text = if (status.replyIndention - magicNumber == 1) {
                                "+"
                            } else {
                                "${status.replyIndention - magicNumber}+"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colorScheme.onSurface
                            )
                        )
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .padding(end = PaddingSize0_5)
                                .width(ThickSm)
                                .background(color = colorScheme.onSurface)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val emojiText = ui?.contentEmojiText
                    val mapping = emojiText?.mapping
                    val text = emojiText?.text
                    var clicked by remember(ui) { mutableStateOf(false) }
                    LaunchedEffect(clicked) {
                        if (clicked) onReplying(false)
                    }

                    val uriHandler = LocalUriHandler.current
                    Box(
                        modifier = Modifier
                            .placeholder(
                                color = colorScheme.surfaceColorAtElevation(
                                    LocalAbsoluteTonalElevation.current + 8.dp
                                ),
                                visible = ui == null,
                                shape = RoundedCornerShape(8.dp),
                                highlight = PlaceholderHighlight.shimmer(
                                    highlightColor = colorScheme.tertiary.copy(alpha = 0.2f)
                                ),
                            )
                    ) {

                        ClickableText(
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = colorScheme.onSurface,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = text ?: buildAnnotatedString { },
                            onClick = {
                                clicked = !clicked
                                if (!clicked && showReply) showReply = false
                                val annotation = text!!
                                    .getStringAnnotations(
                                        tag = "URL", start = it,
                                        end = it
                                    )
                                    .firstOrNull()

                                urlHandlerMediator.givenUrl(
                                    ui = ui,
                                    url = annotation?.item,
                                    isValidUrl = URLUtil::isValidUrl,
                                    openUri = uriHandler::openUri,
                                    goToTag = goToTag,
                                    goToProfile = goToProfile,
                                    goToConversation = goToConversation,
                                )
                            },
                            inlineContent = mapping ?: emptyMap()
                        )
                    }

                    if (ui?.poll?.options != null && ui.poll.options.isNotEmpty()) {
                        Log.d("qqqq", "ui id: ${ui.originalId}, ${ui.remoteId}")
                        PollVoter(
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = colorScheme.onSurface,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth(),
                            poll = ui.poll,
                            options = ui.poll.options,
                            onClick = { choices ->
                                onVote(ui.remoteId, ui.poll.remoteId, choices)
                            },
                        )
                    }

                    ContentImage(ui?.attachments?.mapNotNull { it.url } ?: emptyList()) {
                        clicked = !clicked
                    }
                    val toolbarHeight = PaddingSize6
                    val toolbarHeightPx =
                        with(LocalDensity.current) {
                            toolbarHeight.roundToPx().toFloat()
                        }
                    val toolbarOffsetHeightPx = remember(ui) { mutableStateOf(0f) }
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
                            ui?.mentions?.map { mention -> mention.username }
                                ?.toMutableList() ?: mutableListOf()

                        mentions.add(ui?.userName ?: "")
                        mentions = mentions.map { "@${it}" }.toMutableList()
                        Column(modifier = Modifier.padding(top = PaddingSize2)) {
                            UserInput(
                                ui,
                                account = account,
                                connection = nestedScrollConnection,
                                goToBottomSheet = goToBottomSheet,
                                onMessageSent = { it, visibility, uris ->
                                    ui?.let { it1 ->
                                        replyToStatus(
                                            it,
                                            visibility,
                                            it1.remoteId,
                                            ui.replyCount,
                                            uris
                                        )
                                    }
                                    showReply = false
                                },
                                defaultVisiblity = "Public",
                                participants = mentions.joinToString(" "),
                                showReplies = true,
                                goToConversation = goToConversation,
                                goToProfile = goToProfile,
                                goToTag = goToTag,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.placeholder(
                    color = colorScheme.surfaceColorAtElevation(
                        LocalAbsoluteTonalElevation.current + 8.dp
                    ),
                    visible = ui == null,
                    shape = RoundedCornerShape(8.dp),
                    highlight = PlaceholderHighlight.shimmer(
                        highlightColor = colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                )
            ) {

                ui?.card?.let { card ->
                    ContentCard(
                        card = card,
                    )
                }

                val current = LocalAuthComponent.current
                var justBookmarked by remember { mutableStateOf(false) }

                ButtonBar(
                    status = ui,
                    account = account,
                    replyCount = ui?.replyCount,
                    boostCount = ui?.boostCount,
                    favoriteCount = ui?.favoriteCount,
                    favorited = ui?.favorited,
                    boosted = ui?.boosted,
                    hasParent = ui?.inReplyTo != null,
                    goToBottomSheet = goToBottomSheet,
                    onBoost = {
                        boostStatus(ui!!.remoteId, ui.boosted)
                    },
                    onFavorite = {
                        favoriteStatus(ui!!.remoteId, ui.favorited)
                    },
                    onReply = {
                        showReply = !showReply
                        onReplying(showReply)
                    },
                    onShowReplies = {
                        goToConversation(ui!!)
                    },
                    bookmarked = ui?.bookmarked ?: false || justBookmarked,
                    onBookmark = {
                        justBookmarked = true
                        current.submitPresenter()
                            .handle(SubmitPresenter.BookmarkMessage(ui!!.remoteId, ui.type))
                    }
                )
            }
        }
    }
    Divider()
}


@Composable
fun UserInfo(
    ui: UI?,
    goToProfile: (String) -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit = { a, b -> }
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = PaddingSize1, start = 60.dp)
//            .placeholder(
//                color = colorScheme.surfaceColorAtElevation(
//                    LocalAbsoluteTonalElevation.current + 8.dp
//                ),
//                visible = ui == null,
//                shape = RoundedCornerShape(8.dp),
//                highlight = PlaceholderHighlight.shimmer(
//                    highlightColor = colorScheme.tertiary.copy(alpha = 0.2f)
//                ),
//            )
    ) {
        if (ui?.directMessage != null) {
            DirectMessage(ui.directMessage)
        }
        if (ui?.boostedBy != null)
            Boosted(ui.boostedEmojiText, R.drawable.rocket3,
                ui.boostedAvatar,
                containerColor = colorScheme.surface,
                onClick = {
                    onProfileClick(ui.boostedById!!, true)
                })

    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = PaddingSize1)
            .clickable {
                ui?.accountId?.let { goToProfile(it) }
            },
        horizontalArrangement = Arrangement.Start
    ) {
        AvatarImage(PaddingSize7, ui?.avatar, onClick = { goToProfile(ui?.accountId!!) })
        Column(Modifier.padding(start = PaddingSize1)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PaddingSize0_5)
                    .placeholder(
                        color = colorScheme.surfaceColorAtElevation(
                            LocalAbsoluteTonalElevation.current + 8.dp
                        ),
                        visible = ui == null,
                        shape = RoundedCornerShape(8.dp),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth(.6f)
                        .align(Alignment.Top),
                    text = ui?.accountEmojiText?.text ?: buildAnnotatedString { },
                    style = MaterialTheme.typography.bodyLarge,
                    inlineContent = ui?.accountEmojiText?.mapping ?: emptyMap(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        color = colorScheme.surfaceColorAtElevation(
                            LocalAbsoluteTonalElevation.current + 8.dp
                        ),
                        visible = ui == null,
                        shape = RoundedCornerShape(8.dp),
                        highlight = PlaceholderHighlight.shimmer(
                            highlightColor = colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    color = colorScheme.secondary,
                    text = ui?.userName ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(end = PaddingSize1)
                        .weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    color = colorScheme.secondary,
                    text = ui?.timePosted ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
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

@Composable
fun PollVoter(
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    poll: PollUI,
    options: List<PollHashUI>,
    onClick: (choices: List<Int>) -> Unit,
) {

    var disabled by remember { mutableStateOf(poll.expired || (poll.voted == true && poll.ownVotes != null)) }

    if (poll.multiple) {
        MultipleChoicePollVoter(
            style = style,
            modifier = modifier,
            content = poll.content,
            ownVotes = poll.ownVotes?.toSet() ?: emptySet(),
            options = options,
            disabled = disabled,
            onClick = {
                disabled = true
                onClick(it)
            },
        )
    } else {
        SingleChoicePollVoter(
            style = style,
            modifier = modifier,
            content = poll.content,
            ownVote = poll.ownVotes?.firstOrNull(),
            options = options,
            disabled = disabled,
            onClick = {
                disabled = true
                onClick(listOf(it))
            }
        )
    }
}

@Composable
fun MultipleChoicePollVoter(
    style: TextStyle,
    modifier: Modifier,
    content: String?,
    ownVotes: Set<Int>,
    options: List<PollHashUI>,
    disabled: Boolean,
    onClick: (List<Int>) -> Unit,
) {
    val selected = remember {
        mutableStateListOf(*List(options.size) { index -> ownVotes.contains(index) }.toTypedArray())
    }
    var clicked by remember { mutableStateOf(disabled) }
    val imageSize: Float by animateFloatAsState(
        targetValue = if (clicked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium // with medium speed
        )
    )
    Column(
        modifier = Modifier
            .padding(PaddingSize1)
            .fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            MultiChoicePollOptionVoter(
                modifier = modifier,
                style = style,
                selected = selected[index],
                option = option,
                disabled = disabled,
                onClick = {
                    selected[index] = !selected[index]
                }
            )
        }
        content?.let {
            Text(
                color = colorScheme.secondary,
                style = if (disabled) {
                    style.copy(color = style.color.copy(alpha = ContentAlpha.disabled))
                } else style,
                text = it
            )
        }

        Button(
            modifier = Modifier
                .padding(end = 20.dp, top = 8.dp, bottom = 2.dp)
                .wrapContentSize()
                .align(Alignment.End),
            elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                defaultElevation = PaddingSize3,
                pressedElevation = PaddingSize1,
                disabledElevation = PaddingSize3
            ),
            enabled = !disabled,
            onClick = {
                onClick(selected.mapIndexedNotNull { index, s -> if (s) index else null })
                clicked = true
            },
            shape = CircleShape,
            contentPadding = PaddingValues(PaddingSize1)
        ) {
            Row(Modifier.padding(4.dp)) {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(2f * imageSize)
                        .rotate(imageSize * -45f)
                        .offset(y = (0).dp, x = (2).dp)
                        .rotate(50f)
                        .padding(start = 2.dp, end = 2.dp),
                    painter = painterResource(R.drawable.horn),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(colorScheme.background),
                )
            }

        }
    }
}

@Composable
fun SingleChoicePollVoter(
    style: TextStyle,
    modifier: Modifier,
    content: String?,
    ownVote: Int?,
    options: List<PollHashUI>,
    disabled: Boolean,
    onClick: (Int) -> Unit,
) {
    val selected = remember {
        mutableStateListOf(*List(options.size) { index -> ownVote == index }.toTypedArray())
    }
    Column(
        modifier = Modifier
            .padding(PaddingSize1)
            .fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SingleChoicePollOptionVoter(
                modifier = modifier,
                style = style,
                selected = selected[index],
                option = option,
                disabled = disabled,
                onClick = {
                    selected[index] = !selected[index]
                    onClick(index)
                },
            )
        }
        content?.let {
            Text(
                color = colorScheme.secondary,
                style = if (disabled) {
                    style.copy(color = style.color.copy(alpha = ContentAlpha.disabled))
                } else style,
                text = it
            )
        }
    }
}

@Composable
fun MultiChoicePollOptionVoter(
    modifier: Modifier,
    style: TextStyle,
    option: PollHashUI,
    disabled: Boolean,
    selected: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Checkbox(
            checked = selected,
            modifier = Modifier
                .padding(PaddingSize1)
                .alignByBaseline(),
            enabled = !disabled,
            onCheckedChange = {
                onClick(selected)
            }
        )

        ClickableText(
            text = option.voteContent,
            modifier = Modifier
                .padding(PaddingSize1)
                .weight(1f)
                .alignByBaseline(),
            style = if (disabled) style.copy(color = style.color.copy(alpha = ContentAlpha.disabled)) else style,
            onClick = {
                if (disabled) return@ClickableText
                onClick(selected)
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        if (disabled) {
            Text(
                text = option.percentage,
                modifier = Modifier
                    .padding(PaddingSize1)
                    .alignByBaseline(),
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SingleChoicePollOptionVoter(
    modifier: Modifier,
    style: TextStyle,
    option: PollHashUI,
    disabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        RadioButton(
            selected = selected,
            modifier = Modifier
                .padding(PaddingSize0_5)
                .alignByBaseline(),
            enabled = !disabled,
            onClick = {
                onClick()
            },
        )

        ClickableText(
            text = option.voteContent,
            modifier = Modifier
                .padding(PaddingSize0_5)
                .weight(1f)
                .alignByBaseline(),
            style = if (disabled) style.copy(color = style.color.copy(alpha = ContentAlpha.disabled)) else style,
            onClick = {
                if (disabled) return@ClickableText
                onClick()
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        if (disabled) {
            Text(
                text = option.percentage,
                modifier = Modifier
                    .padding(PaddingSize0_5)
                    .alignByBaseline(),
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
