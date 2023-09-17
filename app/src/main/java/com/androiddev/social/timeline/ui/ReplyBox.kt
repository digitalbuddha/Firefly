package com.androiddev.social.timeline.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.LocalImageLoader
import com.androiddev.social.theme.*
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import social.androiddev.firefly.R


enum class InputSelector {
    NONE,
    MAP,
    REPLIES,
    EMOJI,
    PHONE,
    PICTURE,
    POLL,
}

enum class EmojiStickerSelector {
    EMOJI,
    STICKER
}

data class PostNewMessageUI(
    val content: String,
    val visibility: String,
    val uris: Set<Uri>,
    val replyStatusId: String? = null,
    val replyCount: Int = 0,
    val pollOptions: List<String>? = null,
    val pollExpiresIn: Int = 60 * 60 * 24 * 7, // a week
    val pollMultipleChoices: Boolean = false,
    val pollHideTotals: Boolean = false,
)

@ExperimentalMaterialApi
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UserInput(
    status: UI?,
    account: Account?,
    connection: NestedScrollConnection? = null,
    modifier: Modifier = Modifier,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    onMessageSent: (PostNewMessageUI) -> Unit,
    resetScroll: () -> Unit = {},
    isVerticalScrollHandled: Boolean,
    defaultVisiblity: String = "Public",
    participants: String = " ",
    showReplies: Boolean,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Intercept back navigation if there's a InputSelector visible


    var textState by remember { mutableStateOf(TextFieldValue("$participants ")) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(defaultVisiblity) }

    Surface(
        tonalElevation = 2.dp,
        color = Color.Transparent,
        modifier = modifier
    ) {
        val focusRequester = remember { FocusRequester() }
        val uris = remember { mutableStateListOf<Uri>() }
        val pollOptions = remember { mutableStateListOf<String>() }
        val multiOptionPoll = remember { mutableStateOf(false) }
        val hideTotalsInPoll = remember { mutableStateOf(false) }
        val expireInPoll = remember { mutableStateOf((60 * 60 * 24 * 7).fromExpireInToLocalDatetime()) }  // a week
        val enableAddUris = remember { mutableStateOf(true) }
        val enableAddPoll = remember { mutableStateOf(true) }

        Column(
            modifier = modifier
                .focusRequester(focusRequester)
                .padding(PaddingSizeNone)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.surface.copy(alpha = .9f))
        ) {

            UserInputText(
                textFieldValue = textState,
                onTextChanged = { it: TextFieldValue, selected ->
                    textState = it; visibility = selected
                },
                // Only show the keyboard if there's no input selector and text field has focus
                keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                // Close extended selector if text field receives focus
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
                        resetScroll()
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState,
                defaultVisiblity = visibility,
            )

            UserInputSelector(
                onSelectorChange = { currentInputSelector = it },
                sendMessageEnabled = textState.text.isNotBlank(),

                onMessageSent = {
                    onMessageSent(
                        PostNewMessageUI(
                            content = textState.text,
                            visibility = visibility,
                            uris = uris.toSet(),
                            pollOptions = pollOptions.toList(),
                            pollMultipleChoices = multiOptionPoll.value,
                            pollHideTotals = hideTotalsInPoll.value,
                            pollExpiresIn = expireInPoll.value.toInstant(TimeZone.currentSystemDefault()).toExpireIn(),
                        )
                    )
                    // Reset text field and close keyboard
                    textState = TextFieldValue()
                    pollOptions.clear()
                    multiOptionPoll.value = false
                    hideTotalsInPoll.value = false
                    expireInPoll.value = (60 * 60 * 24 * 7).fromExpireInToLocalDatetime() // a week
                    // Move scroll to bottom
                    resetScroll()
                    dismissKeyboard()
                    keyboardController?.hide()
                },
                currentInputSelector = currentInputSelector,
            )
            SelectorExpanded(
                account = account,
                currentSelector = currentInputSelector,
                onCloseRequested = dismissKeyboard,
                onClearSelector = { currentInputSelector = InputSelector.NONE },
                onTextAdded = { textState = textState.addText(it) },
                connection = connection,
                goToBottomSheet = goToBottomSheet,
                addUri = {
                    uris.add(it)
                    enableAddUris.value = true
                    enableAddPoll.value = false
                },
                pollOptionAdded = {
                    pollOptions.add(it)
                    enableAddUris.value = false
                    enableAddPoll.value = true
                },
                status = status,
                goToConversation = goToConversation,
                goToProfile = goToProfile,
                goToTag=goToTag
            )
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .padding(
                        PaddingSize0_5
                    )
            ) {
                uris.forEach {
                    Box {
                        AsyncImage(
                            model = it.toString(),
                            contentDescription = "Content",
                            imageLoader = LocalImageLoader.current,
                            modifier = modifier
                                .padding(PaddingSize0_5)
                                .clickable {
                                    uris.remove(it)
                                    if (uris.isEmpty()) {
                                        enableAddUris.value = true
                                        enableAddPoll.value = true
                                    }
                                }
                                .size(80.dp),
                            transform = AsyncImagePainter.DefaultTransform,
                            onState = { },
                            alignment = Alignment.Center,
                            contentScale = ContentScale.FillWidth,
                            alpha = androidx.compose.ui.graphics.DefaultAlpha,
                            colorFilter = null,
                            filterQuality = DrawScope.DefaultFilterQuality
                        )
                    }
                }
            }
            val pollColumnModifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(PaddingSize0_5)
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = if (isVerticalScrollHandled) {
                    pollColumnModifier
                } else {
                    pollColumnModifier.verticalScroll(rememberScrollState())
                }
            ) {
                if (pollOptions.isNotEmpty()) {
                    PollExpireAt(
                        expireAt = expireInPoll.value,
                        onClick = {
                            expireInPoll.value = it
                        }
                    )
                    PollProperty(
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.onSurface,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        option = "Multiple choice poll?",
                        selected = multiOptionPoll.value,
                        onClick = {
                            multiOptionPoll.value = !multiOptionPoll.value
                        }
                    )
                    PollProperty(
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.onSurface,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        option = "Hide vote counts until the poll ends?",
                        selected = hideTotalsInPoll.value,
                        onClick = {
                            hideTotalsInPoll.value = !hideTotalsInPoll.value
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(horizontal = PaddingSize2),
                        text = "Added options:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colorScheme.onSurface,
                        )
                    )
                }

                pollOptions.forEach { option ->
                    PollOption(modifier, option, pollOptions)
                }
            }
        }
    }
}

@Composable
private fun PollOption(
    modifier: Modifier,
    option: String,
    pollOptions: SnapshotStateList<String>
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        ClickableText(
            text = buildAnnotatedString { append(option) },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
                .padding(horizontal = PaddingSize3, vertical = PaddingSize1),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorScheme.onSurface,
                lineHeight = 16.sp
            ),
            onClick = {
                pollOptions.remove(option)
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            modifier = Modifier
                .padding(horizontal = PaddingSize1)
                .align(Alignment.CenterVertically),
            onClick = {
                pollOptions.remove(option)
            }
        ) {
            Icon(
                tint = colorScheme.error,
                imageVector = Icons.Filled.Close,
                contentDescription = "delete"
            )
        }
    }
}

private fun TextFieldValue.addText(newString: String): TextFieldValue {
    val newText = this.text.replaceRange(
        this.selection.start,
        this.selection.end,
        newString
    )
    val newSelection = TextRange(
        start = newText.length,
        end = newText.length
    )

    return this.copy(text = newText, selection = newSelection)
}

@ExperimentalMaterialApi
@Composable
private fun SelectorExpanded(
    currentSelector: InputSelector,
    account: Account?,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    onCloseRequested: () -> Unit,
    onClearSelector: () -> Unit,
    onTextAdded: (String) -> Unit,
    connection: NestedScrollConnection?,
    addUri: (Uri) -> Unit = {},
    pollOptionAdded: (String) -> Unit,
    status: UI?,
    goToConversation: (UI) -> Unit = {},
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    if (currentSelector == InputSelector.NONE) return

    // Request focus to force the TextField to lose it
    val focusRequester = FocusRequester()
    // If the selector is shown, always request focus to trigger a TextField.onFocusChange.
    SideEffect {
        if (currentSelector == InputSelector.EMOJI || currentSelector == InputSelector.POLL) {
            focusRequester.requestFocus()
        }
    }

    Surface(tonalElevation = PaddingSize1) {
        when (currentSelector) {
            InputSelector.EMOJI -> EmojiSelector(onTextAdded, focusRequester, connection)
            InputSelector.REPLIES  -> {}
            InputSelector.PICTURE -> PhotoPickerResultComposable(addUri) {
                onClearSelector()
            }
            InputSelector.POLL -> { PollCreator(focusRequester, pollOptionAdded) }
//            InputSelector.MAP -> FunctionalityNotAvailablePanel()
//            InputSelector.PHONE -> FunctionalityNotAvailablePanel()
            else -> {
                throw NotImplementedError()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FunctionalityNotAvailablePanel() {
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {

    }

}

@Composable
fun PhotoPickerResultComposable(addUri: (Uri) -> Unit, clearFocus: () -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.data?.let { it1 -> addUri(it1) }
            clearFocus()
        }

    LaunchedEffect(key1 = Unit) {
        val intent: Intent
//        val usePhotoPicker = isPhotoPickerAvailable()
//        if (usePhotoPicker) {
//            intent = Intent(MediaStore.ACTION_PICK_IMAGES)
//            intent.putExtra(
//                MediaStore.EXTRA_PICK_IMAGES_MAX,
//                8
//            )
//        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
//        }
//
//        if (!usePhotoPicker) {
            // If photo picker is being used these are the default mimetypes.
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
//        }

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        launcher.launch(intent)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollCreator(
    focusRequester: FocusRequester,
    pollOptionAdded: (String) -> Unit,
) {
    var option: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = option,
            onValueChange = {
                option = it
            },
            modifier = Modifier
                .wrapContentHeight()
                .padding(
                    horizontal = PaddingSize1,
                    vertical = PaddingSize2
                )
                .focusRequester(focusRequester)
                .weight(1f),
            label = {
                Text("Type an option for the poll")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (option.text.isNotBlank()) {
                        pollOptionAdded(option.text)
                        option = TextFieldValue("")
                    }
                }
            ),
            maxLines = 1,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface)
        )
        IconButton(
            onClick = {
                if (option.text.isNotBlank()) {
                    pollOptionAdded(option.text)
                    option = TextFieldValue("")
                }
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add",
                tint = colorScheme.primary,
            )
        }
    }
}

@Composable
private fun UserInputSelector(
    onSelectorChange: (InputSelector) -> Unit,
    sendMessageEnabled: Boolean,
    onMessageSent: () -> Unit,
    currentInputSelector: InputSelector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .padding(start = PaddingSizeNone, end = PaddingSize1, bottom = PaddingSize1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.EMOJI) },
            icon = ImageVector.vectorResource(R.drawable.smile),
            selected = currentInputSelector == InputSelector.EMOJI,
            description = "Emoji"
        )

        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.PICTURE) },
            icon = ImageVector.vectorResource(R.drawable.photo),
            selected = currentInputSelector == InputSelector.PICTURE,
            description = "Photo"
        )

        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.POLL) },
            icon = ImageVector.vectorResource(R.drawable.vote),
            selected = currentInputSelector == InputSelector.POLL,
            description = "Poll"
        )
//        AnimatedVisibility(visible = showReplies) {
//            InputSelectorButton(
//                onClick = { onSelectorChange(InputSelector.REPLIES) },
//                icon = ImageVector.vectorResource(R.drawable.reply_all),
//                selected = currentInputSelector == InputSelector.REPLIES,
//                description = "Replies"
//            )
//        }


        val border = if (!sendMessageEnabled) {
            BorderStroke(
                width = ThickSm,
                color = colorScheme.onSurface.copy(alpha = 0.3f)
            )
        } else {
            null
        }
        Spacer(modifier = Modifier.weight(1f))

        val disabledContentColor = colorScheme.primary.copy(alpha = .4f)

        val buttonColors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .4f),
            disabledContentColor = colorScheme.primary,
//            containerColor = MaterialTheme.colorScheme.tertiary
        )
        var clicked by remember { mutableStateOf(false) }

        val imageSize: Float by animateFloatAsState(
            if (clicked) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium // with medium speed
            )
        )
        if (imageSize == 1.1f) clicked = false
        val scope = rememberCoroutineScope()

        Button(
            modifier = Modifier
                .padding(end = 20.dp, top = 8.dp, bottom = 2.dp)
                .wrapContentSize(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = PaddingSize3,
                pressedElevation = PaddingSize1,
                disabledElevation = PaddingSize3
            ),
            enabled = sendMessageEnabled,
            onClick = {
                clicked = !clicked
                scope.launch {
                    onMessageSent()
                }
            },
            colors = buttonColors,
            border = border,
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
private fun InputSelectorButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    selected: Boolean
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            color = colorScheme.primary.copy(alpha = .5f),
            shape = RoundedCornerShape(TouchpointMd)
        )
    } else {
        Modifier
    }
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(TouchpointMd)
//            .then(backgroundModifier)
    ) {
        val tint = if (selected) {
            colorScheme.primary.copy(alpha = .5f)
        } else {
            colorScheme.tertiary.copy(.5f)
        }
        Icon(
            icon,
            tint = tint,
            modifier = Modifier
                .padding(0.dp)
                .size(24.dp),
            contentDescription = description
        )
    }
}

@Composable
private fun NotAvailablePopup(onDismissed: () -> Unit) {
//    FunctionalityNotAvailablePopup(onDismissed)
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue, String) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
    defaultVisiblity: String
) {
    val a11ylabel = "description"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics {
                contentDescription = a11ylabel
                keyboardShownProperty = keyboardShown
            },
        horizontalArrangement = Arrangement.End
    ) {
        Surface {
            Row(modifier = Modifier.background(colorScheme.surfaceVariant.copy(alpha = .9f))) {

                var visibility by remember { mutableStateOf(defaultVisiblity) }

                Box(
                    modifier = Modifier
                        .heightIn(min = 60.dp)
                        .fillMaxWidth(.75f)
                        .align(Alignment.Bottom)
//                        .background(Color.Red)
                ) {
                    var lastFocusState by remember { mutableStateOf(false) }

                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            onTextChanged(it, visibility)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                start = PaddingSize1,
                                bottom = PaddingSize2,
                                top = PaddingSize2
                            )
                            .align(Alignment.TopStart)
                            .onFocusChanged { state ->
                                if (lastFocusState != state.isFocused) {
                                    onTextFieldFocused(state.isFocused)
                                }
                                lastFocusState = state.isFocused
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = ImeAction.None,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        maxLines = 10,
                        minLines = 1,
                        cursorBrush = SolidColor(LocalContentColor.current),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface)
                    )


                    if (textFieldValue.text.isEmpty() && !focusState) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = PaddingSize3,
                                    bottom = PaddingSize2,
                                    top = PaddingSize2
                                )
                                .wrapContentWidth(),
                            text = "Be Heard",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface)
                        )
                    }
                }

                Visibility(visibility) { selected -> visibility = selected }
            }
        }
    }
}

@Composable
fun EmojiSelector(
    onTextAdded: (String) -> Unit,
    focusRequester: FocusRequester,
    connection: NestedScrollConnection?
) {
    var selected by remember { mutableStateOf(EmojiStickerSelector.EMOJI) }

    val a11yLabel = "description"
    Column(
        modifier = Modifier
            .focusRequester(focusRequester) // Requests focus when the Emoji selector is displayed
            // Make the emoji selector focusable so it can steal focus from TextField
            .focusTarget()
            .semantics { contentDescription = a11yLabel }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingSize1)
        ) {
//            ExtendedSelectorInnerButton(
//                text = "Emoji",
//                onClick = { selected = EmojiStickerSelector.EMOJI },
//                selected = true,
//                modifier = Modifier.weight(1f)
//            )
//            ExtendedSelectorInnerButton(
//                text = "Stickers",
//                onClick = { selected = EmojiStickerSelector.STICKER },
//                selected = false,
//                modifier = Modifier.weight(1f)
//            )
        }
        Row(modifier = connection?.let { Modifier.nestedScroll(it) } ?: Modifier) {
            EmojiTable(onTextAdded, modifier = Modifier.padding(PaddingSize1))
        }
    }
    if (selected == EmojiStickerSelector.STICKER) {
        NotAvailablePopup(onDismissed = { selected = EmojiStickerSelector.EMOJI })
    }
}

@Composable
fun ExtendedSelectorInnerButton(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (selected) colorScheme.onSurface.copy(alpha = 0.08f)
        else Color.Transparent,
        disabledContainerColor = Color.Transparent,
        contentColor = colorScheme.onSurface,
//        disabledContentColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.74f)
    )
    TextButton(
        onClick = onClick,
        modifier = modifier
            .padding(PaddingSize1)
            .height(PaddingSize4),
        colors = colors,
        contentPadding = PaddingValues(PaddingSizeNone)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

private fun Int.fromExpireInToLocalDatetime(): LocalDateTime {
    val secondsFromNow: Int = this
    return Instant.fromEpochMilliseconds(
        Clock.System.now().toEpochMilliseconds() + 1000 * secondsFromNow
    ).toLocalDateTime(TimeZone.currentSystemDefault())
}

private fun Instant.toExpireIn(): Int {
    val expirationFromNow = toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()
    return (expirationFromNow / 1000f).toInt()
}

@Composable
private fun PollExpireAt(
    expireAt: LocalDateTime,
    onClick: (LocalDateTime) -> Unit,
) {
    val datetime = remember { mutableStateOf(expireAt) }

    val showTimePicker = showTimePicker(
        initHour = expireAt.hour,
        initMinute = expireAt.minute,
        onSelect = { hour, minute ->
            datetime.value = LocalDateTime(
                year = datetime.value.year,
                month = datetime.value.month,
                dayOfMonth = datetime.value.dayOfMonth,
                hour = hour,
                minute = minute
            )
            onClick(datetime.value)
        }
    )
    val showDatePicker = showDatePicker(
        initYear = expireAt.year,
        initMonth = expireAt.monthNumber,
        initDayOfMonth = expireAt.dayOfMonth,
        onSelect = { year, month, dayOfMonth ->
            datetime.value = LocalDateTime(
                year = year,
                monthNumber = month,
                dayOfMonth = dayOfMonth,
                hour = expireAt.hour,
                minute = expireAt.minute
            )
            showTimePicker.invoke()
        }
    )

    ClickableText(
        text = buildAnnotatedString { append("Expire at(click to change): ${datetime.value.format()}") },

        modifier = Modifier
            .padding(PaddingSize2),
        style = MaterialTheme.typography.titleSmall.copy(
            color = colorScheme.onSurface,
        ),
        onClick = {
                  showDatePicker.invoke()
        },
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun LocalDateTime.format(): String {
    return "$year-$monthNumber-$dayOfMonth $hour:$minute:$second"
}

@Composable
private fun showDatePicker(
    initYear: Int,
    initMonth: Int,
    initDayOfMonth: Int,
    onSelect: (year: Int, month: Int, dayOfMonth: Int) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            onSelect(year, month, dayOfMonth)
        }, initYear, initMonth, initDayOfMonth
    )
    return {
        datePickerDialog.show()
    }
}

@Composable
private fun showTimePicker(
    initHour: Int,
    initMinute: Int,
    onSelect: (hour: Int, minute: Int) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val timePickerDialog = TimePickerDialog(
        context,
        {_, hour : Int, minute: Int ->
            onSelect(hour, minute)
        }, initHour, initMinute, false
    )
    return {
        timePickerDialog.show()
    }
}

@Composable
private fun PollProperty(
    modifier: Modifier,
    style: TextStyle,
    option: String,
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
                .padding(horizontal = PaddingSize1)
                .align(Alignment.CenterVertically),
            onCheckedChange = {
                onClick(selected)
            }
        )

        ClickableText(
            text = buildAnnotatedString { append(option) },
            modifier = Modifier
                .padding(PaddingSize1)
                .weight(1f)
                .alignByBaseline(),
            style = style,
            onClick = {
                onClick(selected)
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
@Composable
fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        repeat(4) { x ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(EMOJI_COLUMNS) { y ->
                    val emoji = emojis[x * EMOJI_COLUMNS + y]
                    Text(
                        modifier = Modifier
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 42.dp, minHeight = PaddingSize5)
                            .padding(PaddingSize1),
                        text = emoji,
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

private const val EMOJI_COLUMNS = 10

private val emojis = listOf(
    "\ud83d\ude00", // Grinning Face
    "\ud83d\ude01", // Grinning Face With Smiling Eyes
    "\ud83d\ude02", // Face With Tears of Joy
    "\ud83d\ude03", // Smiling Face With Open Mouth
    "\ud83d\ude04", // Smiling Face With Open Mouth and Smiling Eyes
    "\ud83d\ude05", // Smiling Face With Open Mouth and Cold Sweat
    "\ud83d\ude06", // Smiling Face With Open Mouth and Tightly-Closed Eyes
    "\ud83d\ude09", // Winking Face
    "\ud83d\ude0a", // Smiling Face With Smiling Eyes
    "\ud83d\ude0b", // Face Savouring Delicious Food
    "\ud83d\ude0e", // Smiling Face With Sunglasses
    "\ud83d\ude0d", // Smiling Face With Heart-Shaped Eyes
    "\ud83d\ude18", // Face Throwing a Kiss
    "\ud83d\ude17", // Kissing Face
    "\ud83d\ude19", // Kissing Face With Smiling Eyes
    "\ud83d\ude1a", // Kissing Face With Closed Eyes
    "\u263a", // White Smiling Face
    "\ud83d\ude42", // Slightly Smiling Face
    "\ud83e\udd17", // Hugging Face
    "\ud83d\ude07", // Smiling Face With Halo
    "\ud83e\udd13", // Nerd Face
    "\ud83e\udd14", // Thinking Face
    "\ud83d\ude10", // Neutral Face
    "\ud83d\ude11", // Expressionless Face
    "\ud83d\ude36", // Face Without Mouth
    "\ud83d\ude44", // Face With Rolling Eyes
    "\ud83d\ude0f", // Smirking Face
    "\ud83d\ude23", // Persevering Face
    "\ud83d\ude25", // Disappointed but Relieved Face
    "\ud83d\ude2e", // Face With Open Mouth
    "\ud83e\udd10", // Zipper-Mouth Face
    "\ud83d\ude2f", // Hushed Face
    "\ud83d\ude2a", // Sleepy Face
    "\ud83d\ude2b", // Tired Face
    "\ud83d\ude34", // Sleeping Face
    "\ud83d\ude0c", // Relieved Face
    "\ud83d\ude1b", // Face With Stuck-Out Tongue
    "\ud83d\ude1c", // Face With Stuck-Out Tongue and Winking Eye
    "\ud83d\ude1d", // Face With Stuck-Out Tongue and Tightly-Closed Eyes
    "\ud83d\ude12", // Unamused Face
    "\ud83d\ude13", // Face With Cold Sweat
    "\ud83d\ude14", // Pensive Face
    "\ud83d\ude15", // Confused Face
    "\ud83d\ude43", // Upside-Down Face
    "\ud83e\udd11", // Money-Mouth Face
    "\ud83d\ude32", // Astonished Face
    "\ud83d\ude37", // Face With Medical Mask
    "\ud83e\udd12", // Face With Thermometer
    "\ud83e\udd15", // Face With Head-Bandage
    "\u2639", // White Frowning Face
    "\ud83d\ude41", // Slightly Frowning Face
    "\ud83d\ude16", // Confounded Face
    "\ud83d\ude1e", // Disappointed Face
    "\ud83d\ude1f", // Worried Face
    "\ud83d\ude24", // Face With Look of Triumph
    "\ud83d\ude22", // Crying Face
    "\ud83d\ude2d", // Loudly Crying Face
    "\ud83d\ude26", // Frowning Face With Open Mouth
    "\ud83d\ude27", // Anguished Face
    "\ud83d\ude28", // Fearful Face
    "\ud83d\ude29", // Weary Face
    "\ud83d\ude2c", // Grimacing Face
    "\ud83d\ude30", // Face With Open Mouth and Cold Sweat
    "\ud83d\ude31", // Face Screaming in Fear
    "\ud83d\ude33", // Flushed Face
    "\ud83d\ude35", // Dizzy Face
    "\ud83d\ude21", // Pouting Face
    "\ud83d\ude20", // Angry Face
    "\ud83d\ude08", // Smiling Face With Horns
    "\ud83d\udc7f", // Imp
    "\ud83d\udc79", // Japanese Ogre
    "\ud83d\udc7a", // Japanese Goblin
    "\ud83d\udc80", // Skull
    "\ud83d\udc7b", // Ghost
    "\ud83d\udc7d", // Extraterrestrial Alien
    "\ud83e\udd16", // Robot Face
    "\ud83d\udca9", // Pile of Poo
    "\ud83d\ude3a", // Smiling Cat Face With Open Mouth
    "\ud83d\ude38", // Grinning Cat Face With Smiling Eyes
    "\ud83d\ude39", // Cat Face With Tears of Joy
    "\ud83d\ude3b", // Smiling Cat Face With Heart-Shaped Eyes
    "\ud83d\ude3c", // Cat Face With Wry Smile
    "\ud83d\ude3d", // Kissing Cat Face With Closed Eyes
    "\ud83d\ude40", // Weary Cat Face
    "\ud83d\ude3f", // Crying Cat Face
    "\ud83d\ude3e", // Pouting Cat Face
    "\ud83d\udc66", // Boy
    "\ud83d\udc67", // Girl
    "\ud83d\udc68", // Man
    "\ud83d\udc69", // Woman
    "\ud83d\udc74", // Older Man
    "\ud83d\udc75", // Older Woman
    "\ud83d\udc76", // Baby
    "\ud83d\udc71", // Person With Blond Hair
    "\ud83d\udc6e", // Police Officer
    "\ud83d\udc72", // Man With Gua Pi Mao
    "\ud83d\udc73", // Man With Turban
    "\ud83d\udc77", // Construction Worker
    "\u26d1", // Helmet With White Cross
    "\ud83d\udc78", // Princess
    "\ud83d\udc82", // Guardsman
    "\ud83d\udd75", // Sleuth or Spy
    "\ud83c\udf85", // Father Christmas
    "\ud83d\udc70", // Bride With Veil
    "\ud83d\udc7c", // Baby Angel
    "\ud83d\udc86", // Face Massage
    "\ud83d\udc87", // Haircut
    "\ud83d\ude4d", // Person Frowning
    "\ud83d\ude4e", // Person With Pouting Face
    "\ud83d\ude45", // Face With No Good Gesture
    "\ud83d\ude46", // Face With OK Gesture
    "\ud83d\udc81", // Information Desk Person
    "\ud83d\ude4b", // Happy Person Raising One Hand
    "\ud83d\ude47", // Person Bowing Deeply
    "\ud83d\ude4c", // Person Raising Both Hands in Celebration
    "\ud83d\ude4f", // Person With Folded Hands
    "\ud83d\udde3", // Speaking Head in Silhouette
    "\ud83d\udc64", // Bust in Silhouette
    "\ud83d\udc65", // Busts in Silhouette
    "\ud83d\udeb6", // Pedestrian
    "\ud83c\udfc3", // Runner
    "\ud83d\udc6f", // Woman With Bunny Ears
    "\ud83d\udc83", // Dancer
    "\ud83d\udd74", // Man in Business Suit Levitating
    "\ud83d\udc6b", // Man and Woman Holding Hands
    "\ud83d\udc6c", // Two Men Holding Hands
    "\ud83d\udc6d", // Two Women Holding Hands
    "\ud83d\udc8f" // Kiss
)

