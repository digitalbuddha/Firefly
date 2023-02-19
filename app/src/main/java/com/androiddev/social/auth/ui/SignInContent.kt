package com.androiddev.social.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.ThickMd

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    oauthAuthorizeUrl: String,
    error: String?,
    onErrorFromOAuth: (message: String) -> Unit,
    onCloseClicked: () -> Unit,
    shouldCancelLoadingUrl: (url: String) -> Boolean
) {

    Column(
        modifier = modifier
    ) {
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.surface.copy(
                alpha = .9f
            ),
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text("")
            },
            navigationIcon = {
//                Icon(
//                    modifier = Modifier.clickable {
//                        TODO("")
//                    },
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                )
            }
        )

        AnimatedVisibility(error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally)
                    .padding(PaddingSize2)
                    .background(color = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f))
                    .border(
                        border = BorderStroke(
                            width = ThickMd,
                            color =
                            MaterialTheme.colorScheme.tertiary
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(PaddingSize2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Something is wrong!",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        if (oauthAuthorizeUrl.isNotEmpty()) {
            SignInWebView(
                url = oauthAuthorizeUrl,
                modifier = Modifier.fillMaxSize(),
                shouldCancelLoadingUrl = shouldCancelLoadingUrl,
                onWebError = onErrorFromOAuth,
                onCancel = onCloseClicked,
            )
        }
    }
}