package com.androiddev.social.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.R


@ExperimentalComposeUiApi
@Composable
fun NotifIcon() {
    IconButton(
        onClick = {  }) {
        Icon(modifier = Modifier.size(36.dp),
            imageVector = Icons.Filled.Notifications,
            contentDescription = "search"
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun SearchBar(
    searchText: String,
    placeholderText: String = "",
    onSearchTextChanged: (String) -> Unit = {},
    onClearClick: () -> Unit = {},
    onImageOnly: () -> Unit = {},
    onLinksOnly: () -> Unit = {},
    onBoostedOnly: () -> Unit = {},
) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    Column {
        Row {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .onFocusChanged { focusState ->
                        showClearButton = (focusState.isFocused)
                    }
                    .focusRequester(focusRequester),
                value = searchText,
                onValueChange = onSearchTextChanged,
                placeholder = {
                    Text(text = placeholderText)
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    AnimatedVisibility(
                        visible = showClearButton,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { onClearClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "search"
                            )
                        }

                    }
                },
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
            )


            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier.padding(8.dp),

                onClick = onImageOnly
            ) {
                Image(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = onLinksOnly
            ) {
                Image(
                    painter = painterResource(R.drawable.media),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = onBoostedOnly
            ) {
                Image(
                    painter = painterResource(R.drawable.link),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
            IconButton(
                modifier = Modifier.padding(8.dp),
                onClick = onBoostedOnly
            ) {
                Image(
                    painter = painterResource(R.drawable.rocket3),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
        }
    }
}

