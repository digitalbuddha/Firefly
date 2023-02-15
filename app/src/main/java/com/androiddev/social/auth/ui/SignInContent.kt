package com.androiddev.social.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text("Login")
            },
            navigationIcon = {
                Icon(
                    modifier = Modifier.clickable {
                        TODO("")
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                )
            }
        )

        AnimatedVisibility(error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .background(color = MaterialTheme.colorScheme.error.copy(alpha = 0.5F))
                    .border(
                        border = BorderStroke(
                            width = 2.dp,
                            color =
                            MaterialTheme.colorScheme.error
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(16.dp),
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