package com.androiddev.social.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.FireflyTheme

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
