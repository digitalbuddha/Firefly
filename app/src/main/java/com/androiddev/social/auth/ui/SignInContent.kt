package com.androiddev.social.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.FireflyTheme
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
    FireflyTheme {
        Box(Modifier.heightIn(min = 1.dp))
        Column(
            modifier = modifier.wrapContentHeight().background(Color.Transparent)
        ) {
            AnimatedVisibility(error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                        .padding(PaddingSize2)
//                    .background(color = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f))
                        .border(
                            border = BorderStroke(
                                width = ThickMd,
                                color =
                                MaterialTheme.colorScheme.primary
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
}